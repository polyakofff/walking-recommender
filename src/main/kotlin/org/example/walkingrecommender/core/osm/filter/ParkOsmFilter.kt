package org.example.walkingrecommender.core.osm.filter

import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmRelationDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.springframework.stereotype.Component

@Component
class ParkOsmFilter {

    fun filterParks(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isPark(it) },
            relations = data.relations.filter { isPark(it) },
        )
    }

    private fun isPark(way: OsmWayDto): Boolean {
        return way.tags["leisure"] == "park"
    }

    private fun isPark(relation: OsmRelationDto): Boolean {
        return relation.tags["leisure"] == "park"
    }
}