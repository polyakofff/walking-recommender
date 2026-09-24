package org.example.walkingrecommender.urban

import org.locationtech.jts.geom.Geometry

enum class OsmElementType {
    NODE,
    WAY,
    RELATION,
}

enum class UrbanObjectType {
    PARK,            // V
    FOREST,          // V
    WATER,           // V
    INDUSTRIAL_AREA, // V
    GARAGES,         // V
    RAILWAY,         // V
    BUILDING,        // V
    ROAD,            // V
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