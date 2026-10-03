package org.example.walkingrecommender.core.task

import org.example.walkingrecommender.core.model.EdgeSegment
import org.example.walkingrecommender.core.feature.FeatureCalculator
import org.example.walkingrecommender.core.model.SegmentFeatures
import org.example.walkingrecommender.core.db.GraphRepository
import org.example.walkingrecommender.core.db.SegmentsWithEdges
import org.example.walkingrecommender.core.db.UrbanObjectRepository
import org.example.walkingrecommender.core.model.toSegmentId
import org.springframework.stereotype.Component

@Component
class CalculateSegmentsTask(
    private val graphRepository: GraphRepository,
    private val urbanObjectRepository: UrbanObjectRepository,
    private val featureCalculator: FeatureCalculator,
) {

    val version = 1
    val radiusMeters = 300.0

    fun run() {
        var iteration = 0
        while (true) {
            println("iteration: $iteration")

            val data = graphRepository.findSegmentsWithVersionLessThan(version, limit = 100_000)

            if (data.segments.isEmpty()) {
                break
            }

            val calculatedSegments = calculateData(data)

            graphRepository.updateSegmentsFeatures(calculatedSegments)

            iteration++
        }
    }

    private fun calculateData(data: SegmentsWithEdges): List<EdgeSegment> {
        println("segments: ${data.segments.size}")

        return data.segments
            .chunked(1000)
            .flatMapIndexed { batchIndex, segments ->
                val segmentIds = segments.map { it.toSegmentId() }

                val withinObjects = urbanObjectRepository.findWithinObjects(
                    segmentIds = segmentIds,
                    radiusMeters = radiusMeters,
                )
                val totalObjects = withinObjects.values.sumOf { it.size }
                println("batch: ${batchIndex}, total objects: $totalObjects")

                segments.map { segment ->
                    val features = featureCalculator.calculateSegment(
                        edge = data.edges[segment.edgeId]!!,
                        segment = segment,
                        withinObjects = withinObjects[segment.toSegmentId()].orEmpty(),
                    )
                    segment.copy(
                        features = SegmentFeatures(
                            greenery = features.greenery,
                            water = features.water,
                            traffic = features.traffic,
                            noise = features.noise,
                            historicArchitecture = features.historicArchitecture,
                            modernArchitecture = features.modernArchitecture,
                            commercialActivity = features.commercialActivity,
                            grittiness = features.grittiness,
                            pedestrianComfort = features.pedestrianComfort,
                        ),
                        featuresVersion = version,
                    )
                }
            }
    }
}