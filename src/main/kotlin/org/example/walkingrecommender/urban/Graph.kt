package org.example.walkingrecommender.urban

import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point

data class Node(
    val id: Long,
    val nodeOsmId: Long,
    val geometry: Point,
)

fun Node.externalId(): Long = nodeOsmId

data class Edge(
    val id: Long,
    val nodeAOsmId: Long,
    val nodeBOsmId: Long,
    val wayOsmId: Long,
    val geometry: LineString,
    val highway: String,
    val surface: String?,
    val name: String?,
    val lanes: Int?,
    val lit: Boolean,
    val distance: Int,
    val walkingTime: Int,

    val segments: List<EdgeSegment>? = null,
)

data class EdgeSegment(
    val edgeId: Long,
    val index: Int,
    val geometry: LineString,
    val distanceMeters: Double,
    val features: SegmentFeatures? = null,
    val featuresVersion: Int? = null,
)

data class SegmentFeatures(
    val greenery: Double = 0.0,
    val water: Double = 0.0,

    val traffic: Double = 0.0,
    val noise: Double = 0.0,

    val historicArchitecture: Double = 0.0,
    val modernArchitecture: Double = 0.0,

    val commercialActivity: Double = 0.0,
    val grittiness: Double = 0.0,

    val pedestrianComfort: Double = 0.0,
)

data class SegmentId(
    val edgeId: Long,
    val segmentIndex: Int,
)

fun EdgeSegment.toSegmentId(): SegmentId = SegmentId(
    edgeId = this.edgeId,
    segmentIndex = this.index,
)

data class Graph(
    val nodes: Map<Long, Node>,
    val edges: List<Edge>,
)