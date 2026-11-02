package org.example.walkingrecommender.core.model

import org.locationtech.jts.geom.Geometry

enum class OsmElementType {
    NODE,
    WAY,
    RELATION,
}

enum class UrbanObjectType {
    PARK,
    FOREST,
    WATER,
    INDUSTRIAL_AREA,
    GARAGES,
    RAILWAY,
    BUILDING,
    ROAD,
}

data class UrbanObject(
    val id: Long,
    val osmElementType: OsmElementType,
    val osmId: Long,
    val urbanObjectType: UrbanObjectType,
    val geometry: Geometry,
    val name: String?,
    val tags: Map<String, String>,
) {
    override fun toString(): String {
        return "UrbanObject(id=$id, osmElementType=$osmElementType, osmId=$osmId, urbanObjectType=$urbanObjectType, name=$name, tags=$tags, geometry=$geometry)"
    }
}