package org.example.walkingrecommender.core.osm.filter

import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmRelationDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.springframework.stereotype.Component

@Component
class RailwayOsmFilter {

    fun filterRailway(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isRailway(it) },
            relations = data.relations.filter { isRailway(it) },
        )
    }

    private fun isRailway(way: OsmWayDto): Boolean {
        return way.tags["landuse"] == "railway"
    }

    private fun isRailway(relation: OsmRelationDto): Boolean {
        return relation.tags["landuse"] == "railway"
    }
}