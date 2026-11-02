package org.example.walkingrecommender.core.osm

data class OsmNodeDto(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>,
)

data class OsmWayDto(
    val id: Long,
    val nodeIds: List<Long>,
    val tags: Map<String, String>,
)

data class OsmMember(
    val wayId: Long,
    val inner: Boolean,
)

data class OsmRelationDto(
    val id: Long,
    val members: List<OsmMember>,
    val tags: Map<String, String>,
)

data class OsmData(
    val nodesMap: Map<Long, OsmNodeDto>,
    val waysMap: Map<Long, OsmWayDto>,
    val nodes: List<OsmNodeDto>,
    val ways: List<OsmWayDto>,
    val relations: List<OsmRelationDto>,
)