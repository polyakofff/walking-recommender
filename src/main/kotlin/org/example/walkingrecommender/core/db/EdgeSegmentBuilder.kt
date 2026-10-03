package org.example.walkingrecommender.core.db

import org.example.walkingrecommender.WalkingRecommenderProperties
import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.EdgeSegment
import org.example.walkingrecommender.core.GeometryUtils
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