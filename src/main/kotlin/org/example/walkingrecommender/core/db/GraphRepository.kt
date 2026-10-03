package org.example.walkingrecommender.core.db

import org.example.walkingrecommender.core.model.EdgeSegment
import org.example.walkingrecommender.core.model.Graph
import org.locationtech.jts.geom.Point

interface GraphRepository {

    fun save(graph: Graph)

    fun updateSegmentsFeatures(segments: List<EdgeSegment>)

    fun findSegmentsWithVersionLessThan(version: Int, limit: Int = 10_000): SegmentsWithEdges

    fun findNearestNode(point: Point): NearestNode?

    fun findSubgraph(center: Point, radiusMeters: Double): Graph
}