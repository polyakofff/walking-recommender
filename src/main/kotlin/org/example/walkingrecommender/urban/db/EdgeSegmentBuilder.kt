package org.example.walkingrecommender.urban.db

import org.example.walkingrecommender.WalkingRecommenderProperties
import org.example.walkingrecommender.urban.Edge
import org.example.walkingrecommender.urban.EdgeSegment
import org.example.walkingrecommender.urban.GeometryUtils
import org.springframework.stereotype.Component

@Component
class EdgeSegmentBuilder(
    private val geometryUtils: GeometryUtils,
    private val properties: WalkingRecommenderProperties,
) {

    fun build(edge: Edge): List<EdgeSegment> {
        return geometryUtils
            .splitLineStringByMeters(
                lineString = edge.geometry,
                maxMeters = properties.edgeSegmentMaxMeters,
            )
            .mapIndexed { index, lineSegment ->
                EdgeSegment(
                    edgeId = edge.id,
                    index = index,
                    geometry = lineSegment.lineString,
                    distanceMeters = lineSegment.lengthMeters,
                )
            }
    }
}