package org.example.walkingrecommender.core.osm.filter

import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmRelationDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.springframework.stereotype.Component

@Component
class GaragesOsmFilter {

    fun filterGarages(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isGarages(it) },
            relations = data.relations.filter { isGarages(it) },
        )
    }

    private fun isGarages(way: OsmWayDto): Boolean {
        return way.tags["landuse"] == "garages"
    }

    private fun isGarages(relation: OsmRelationDto): Boolean {
        return relation.tags["landuse"] == "garages"
    }
}