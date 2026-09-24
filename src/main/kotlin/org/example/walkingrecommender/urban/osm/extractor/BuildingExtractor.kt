package org.example.walkingrecommender.urban.osm.extractor

import org.example.walkingrecommender.urban.UrbanObject
import org.example.walkingrecommender.urban.UrbanObjectType
import org.example.walkingrecommender.urban.osm.OsmData
import org.example.walkingrecommender.urban.osm.filter.BuildingOsmFilter
import org.springframework.stereotype.Component

@Component
class BuildingExtractor(
    geometryBuilder: GeometryBuilder,
    private val buildingOsmFilter: BuildingOsmFilter,
) : UrbanObjectExtractor(geometryBuilder) {

    override fun filterData(data: OsmData): OsmData {
        return buildingOsmFilter.filterBuilding(data)
    }

    override fun filterUrbanObject(obj: UrbanObject): Boolean {
        return true
    }

    override fun getUrbanObjectType(): UrbanObjectType = UrbanObjectType.BUILDING

    override fun isClosedWay(): Boolean = true

    override fun getName(tags: Map<String, String>): String? {
        return when (tags["building"]) {
            "apartments",
            "yes" -> "${tags["addr:street"]} ${tags["addr:housenumber"]}"
            "commercial",
            "retail" -> tags["name"] ?: "${tags["addr:street"]} ${tags["addr:housenumber"]}"
            else -> null
        }
    }
}