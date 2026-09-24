package org.example.walkingrecommender.urban.osm.filter

import org.example.walkingrecommender.urban.osm.OsmData
import org.example.walkingrecommender.urban.osm.OsmWayDto
import org.springframework.stereotype.Component
import kotlin.collections.contains

@Component
class WalkableOsmFilter {

    fun filterByWalkable(data: OsmData): OsmData {
        return data.copy(
            ways = data.ways.filter { isWalkable(it) }
        )
    }

    fun isWalkable(way: OsmWayDto): Boolean {
        if ("highway" !in way.tags) {
            return false
        }
        var ok = way.tags["highway"] in WALKABLE_HIGHWAYS || way.tags["highway"] in DRIVABLE_HIGHWAYS
//        if (way.tags["highway"] == "service") {
//            ok = ok && way.tags["living_street"] == "yes"
//        }
        if (way.tags["highway"] == "cycleway") {
            ok = ok && way.tags["foot"] == "yes"
        }
        return ok
    }

    companion object {
        private val WALKABLE_HIGHWAYS = setOf(
            "footway", // пешеходные дорожки/тротуары (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dfootway)
            "path", // тропы (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dpath)
            "steps", // ступеньки (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dsteps)
            "pedestrian", // пешеходные улицы (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dpedestrian)
            "track", // лесные дороги (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtrack)
            "cycleway", // велодорожка, для пешеходов должно быть foot=yes (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dcycleway)

            "residential", // дороги внутри жилых зон (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dresidential)
            "living_street", // как residential, но с преимуществом пешеходов (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dliving_street)

//            "service", // служебные проезды (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dservice)

            "unclassified", // непонятные дороги (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dunclassified)
        )

        private val DRIVABLE_HIGHWAYS = setOf(
            "primary", // центральные магистрали города (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dprimary)
            "primary_link", // съезды на primary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dprimary_link)
            "secondary", // основные магистрали районов города (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dsecondary)
            "secondary_link", // съезды на secondary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dsecondary_link)
            "tertiary", // микрорайонные улицы (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtertiary)
            "tertiary_link", // съезды на tertiary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtertiary_link)

            "residential", // дороги внутри жилых зон (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dresidential)
            "living_street", // как residential, но с преимуществом пешеходов (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dliving_street)

//            "service", // служебные проезды (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dservice)

            "unclassified", // непонятные дороги (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dunclassified)
        )
    }
}