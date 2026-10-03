package org.example.walkingrecommender.core.osm.filter

import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmRelationDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.springframework.stereotype.Component

@Component
class WaterOsmFilter {

    fun filterWater(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isWater(it) },
            relations = data.relations.filter { isWater(it) },
        )
    }

    private fun isWater(way: OsmWayDto): Boolean {
        return way.tags["natural"] == "water"
    }

    private fun isWater(relation: OsmRelationDto): Boolean {
        return relation.tags["natural"] == "water"
    }
}