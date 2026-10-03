package org.example.walkingrecommender.core.osm.filter

import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmRelationDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.springframework.stereotype.Component

@Component
class IndustrialOsmFilter {

    fun filterIndustrial(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isIndustrial(it) },
            relations = data.relations.filter { isIndustrial(it) },
        )
    }

    private fun isIndustrial(way: OsmWayDto): Boolean {
        return way.tags["landuse"] == "industrial"
    }

    private fun isIndustrial(relation: OsmRelationDto): Boolean {
        return relation.tags["landuse"] == "industrial"
    }
}