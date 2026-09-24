package org.example.walkingrecommender.urban.osm.filter

import org.example.walkingrecommender.urban.osm.OsmData
import org.springframework.stereotype.Component

@Component
class BuildingOsmFilter {

    fun filterBuilding(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isBuilding(it.tags) },
            relations = data.relations.filter { isBuilding(it.tags) },
        )
    }

    private fun isBuilding(tags: Map<String, String>): Boolean {
        if ("building" !in tags) {
            return false
        }
        var ok = tags["building"] in SIGNIFICANT_BUILDINGS
        if (tags["building"] == "yes") {
            ok = ok && "addr:street" in tags && "addr:housenumber" in tags
        }
        return ok
    }

    companion object {
        val SIGNIFICANT_BUILDINGS = setOf(
            "apartments", // многоквартирный дом
//            "cathedral", // собор
//            "church", // церковь
            "commercial", // место деловой активности
            "retail", // здание розничной торговли
            "yes", // какое-то здание, нужен фильтр
        )
    }
}