package org.example.walkingrecommender.core.osm.filter

import org.example.walkingrecommender.core.osm.OsmData
import org.springframework.stereotype.Component

@Component
class RoadOsmFilter {

    fun filterRoad(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isRoad(it.tags) },
            relations = emptyList(),
        )
    }

    private fun isRoad(tags: Map<String, String>): Boolean {
        if ("highway" !in tags) {
            return false
        }
        return tags["highway"] in DRIVABLE_HIGHWAYS
    }

    companion object {
        val DRIVABLE_HIGHWAYS = setOf(
            "motorway", // автомагистрали (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dmotorway)
            "trunk", // важные дороги не автомагистрали (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtrunk)

            "primary", // центральные магистрали города (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dprimary)
            "primary_link", // съезды на primary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dprimary_link)
            "secondary", // основные магистрали районов города (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dsecondary)
            "secondary_link", // съезды на secondary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dsecondary_link)
            "tertiary", // микрорайонные улицы (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtertiary)
            "tertiary_link", // съезды на tertiary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtertiary_link)

            "residential", // дороги внутри жилых зон (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dresidential)
            "living_street", // как residential, но с преимуществом пешеходов (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dliving_street)

            "unclassified", // непонятные дороги (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dunclassified)
        )
    }
}