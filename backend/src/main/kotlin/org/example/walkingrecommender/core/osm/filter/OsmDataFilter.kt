package org.example.walkingrecommender.core.osm.filter

import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmNodeDto
import org.example.walkingrecommender.core.osm.OsmRelationDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.springframework.stereotype.Component

@Component
class OsmDataFilter {

    fun filterByWalkingGraph(data: OsmData): OsmData {
        return data.copy(
            ways = data.ways.filter { isInWalkingGraph(it) }
        )
    }

    fun isInWalkingGraph(way: OsmWayDto): Boolean {
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

    fun filterByDrivable(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isDrivable(it) },
            relations = emptyList(),
        )
    }

    fun filterByNatural(data: OsmData): OsmData {
        return data.copy(
            ways = data.ways.filter { isNatural(it) },
            relations = data.relations.filter { isNatural(it) },
        )
    }

    private fun isNatural(way: OsmWayDto): Boolean {
        if ("natural" !in way.tags) {
            return false
        }
        return way.tags["natural"] in NATURAL
    }

    private fun isNatural(relation: OsmRelationDto): Boolean {
        if ("natural" !in relation.tags) {
            return false
        }
        return relation.tags["natural"] in NATURAL
    }

    fun filterByLeisure(data: OsmData): OsmData {
        return data.copy(
            ways = data.ways.filter { isLeisure(it) },
            relations = data.relations.filter { isLeisure(it) },
        )
    }

    private fun isLeisure(way: OsmWayDto): Boolean {
        if ("leisure" !in way.tags) {
            return false
        }
        return way.tags["leisure"] in LEISURE
    }

    private fun isLeisure(relation: OsmRelationDto): Boolean {
        if ("leisure" !in relation.tags) {
            return false
        }
        return relation.tags["leisure"] in LEISURE
    }

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

    fun filterByLanduse(data: OsmData): OsmData {
        return data.copy(
            nodes = emptyList(),
            ways = data.ways.filter { isLanduse(it) },
            relations = data.relations.filter { isLanduse(it) },
        )
    }

    private fun isLanduse(way: OsmWayDto): Boolean {
        if ("landuse" !in way.tags) {
            return false
        }
        return way.tags["landuse"] in LANDUSE
    }

    private fun isLanduse(relation: OsmRelationDto): Boolean {
        if ("landuse" !in relation.tags) {
            return false
        }
        return relation.tags["landuse"] in LANDUSE
    }

    fun filterByTag(data: OsmData, tag: String): OsmData {
        return data.copy(
            nodesMap = data.nodesMap,
            waysMap = data.waysMap,
            nodes = data.nodes.filter { hasTag(it, tag) },
            ways = data.ways.filter { hasTag(it, tag) },
            relations = data.relations.filter { hasTag(it, tag) },
        )
    }

    private fun hasTag(node: OsmNodeDto, tag: String): Boolean {
        return tag in node.tags
    }

    private fun hasTag(way: OsmWayDto, tag: String): Boolean {
        return tag in way.tags
    }

    private fun hasTag(relation: OsmRelationDto, tag: String): Boolean {
        return tag in relation.tags
    }

    fun isWalkable(way: OsmWayDto): Boolean {
        if ("highway" !in way.tags) {
            return false
        }
        var ok = way.tags["highway"] in WALKABLE_HIGHWAYS
//        if (way.tags["highway"] == "service") {
//            ok = ok && way.tags["living_street"] == "yes"
//        }
        if (way.tags["highway"] == "cycleway") {
            ok = ok && way.tags["foot"] == "yes"
        }
        return ok
    }

    fun isDrivable(way: OsmWayDto): Boolean {
        if ("highway" !in way.tags) {
            return false
        }
        return way.tags["highway"] in DRIVABLE_HIGHWAYS
    }

    fun isStreet(way: OsmWayDto): Boolean {
        return "highway" in way.tags && "name" in way.tags
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
            "motorway", // автомагистрали (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dmotorway)
            "trunk", // важные дороги не автомагистрали (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtrunk)

            "primary", // центральные магистрали города (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dprimary)
            "primary_link", // съезды на primary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dprimary_link)
            "secondary", // основные магистрали районов города (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dsecondary)
            "secondary_link", // съезды на secondary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dsecondary_link)
            "tertiary", // микрорайонные улицы (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtertiary)
            "tertiary_link", // съезды на tertiary (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dtertiary_link)

//            "residential", // дороги внутри жилых зон (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dresidential)
//            "living_street", // как residential, но с преимуществом пешеходов (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dliving_street)

//            "service", // служебные проезды (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dservice)

            "unclassified", // непонятные дороги (https://wiki.openstreetmap.org/wiki/RU:Tag:highway%3Dunclassified)
        )

        private val NATURAL = setOf(
            "water",
            "wood",
        )

        private val LEISURE = setOf(
            "park",
        )

        private val LANDUSE = setOf(
//            "allotments", // земельные участки
//            "brownfield", // заброшенная территория
//            "cemetery", // кладбище
//            "commercial", // коммерческая зона
//            "construction", // стройка
//            "flowerbed", // клумба
            "garages", // гаражи
//            "grass", // трава
//            "greenfield", // незастроенная территория
            "industrial", // промзона
//            "landfill", // свалка
//            "logging", // вырубка
//            "meadow", // луг
//            "military", // военка
//            "plant_nursery", // питомники растений
            "railway", // железная дорога
//            "recreation_ground", // площадка для отдыха
//            "religious", // религиозная территория
//            "residential", // жилая территория
//            "retail", // магазины
//            "village_green", // зеленая зона
        )
    }
}