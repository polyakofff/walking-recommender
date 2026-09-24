package org.example.walkingrecommender.urban.db

import org.example.walkingrecommender.urban.EdgeSegment
import org.example.walkingrecommender.urban.Graph
import org.locationtech.jts.geom.Point

interface GraphRepository {

    fun save(graph: Graph)

    fun updateSegmentsFeatures(segments: List<EdgeSegment>)

    fun findSegmentsWithVersionLessThan(version: Int, limit: Int = 10_000): SegmentsWithEdges

    fun findNearestNode(point: Point): NearestNode?

    fun findSubgraph(center: Point, radiusMeters: Double): Graph
}