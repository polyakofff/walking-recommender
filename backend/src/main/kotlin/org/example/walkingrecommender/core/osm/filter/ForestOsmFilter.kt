package org.example.walkingrecommender.core.osm.filter

import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmRelationDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.springframework.stereotype.Component

@Component
class ForestOsmFilter {

    fun filterForests(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isForest(it) },
            relations = data.relations.filter { isForest(it) },
        )
    }

    private fun isForest(way: OsmWayDto): Boolean {
        return way.tags["natural"] == "wood"
    }

    private fun isForest(relation: OsmRelationDto): Boolean {
        return relation.tags["natural"] == "wood"
    }
}