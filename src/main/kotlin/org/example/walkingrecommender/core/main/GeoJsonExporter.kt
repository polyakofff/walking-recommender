package org.example.walkingrecommender.core.main

import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.UrbanObject
import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmNodeDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.example.walkingrecommender.core.routing.CalculatedRoute
import org.example.walkingrecommender.core.routing.Route
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.awt.Color
import java.io.File

class GeoJsonExporter {

    private val objectMapper = jacksonObjectMapper()

//    fun export(
//        data: OsmData,
//        isInGraph: (OsmWayDto) -> Boolean,
//        isWalkable: (OsmWayDto) -> Boolean,
//        isDrivable: (OsmWayDto) -> Boolean,
//        isStreet: (OsmWayDto) -> Boolean,
//        file: File
//    ) {
//        val features = data.ways.mapNotNull { (_, way) ->
//            val coordinates = way.nodeIds
//                .mapNotNull { data.nodes[it] }
//                .map { node ->
//                    listOf(node.lon, node.lat)
//                }
//
//            if (coordinates.size < 2) {
//                return@mapNotNull null
//            }
//
//            val inGraph = isInGraph(way)
//            val walkable = isWalkable(way)
//            val drivable = isDrivable(way)
//            val street = isStreet(way)
//
//            mapOf(
//                "type" to "Feature",
//
//                "properties" to mapOf(
//                    "osmWayId" to way.id,
//                    "name" to way.tags["name"],
//                    "highway" to way.tags["highway"],
//
//                    "inGraph" to inGraph,
//                    "walkable" to walkable,
//                    "drivable" to drivable,
//                    "street" to street,
//
//                    "stroke" to if (inGraph) "#1E52D4" else "#AA0000",
//                    "stroke-width" to if (inGraph) 4 else 2,
//                    "stroke-opacity" to if (inGraph) 1.0 else 0.4
//                ),
//
//                "geometry" to mapOf(
//                    "type" to "LineString",
//                    "coordinates" to coordinates
//                )
//            )
//        }
//
//        val geoJson = mapOf(
//            "type" to "FeatureCollection",
//            "features" to features
//        )
//
//        objectMapper
//            .writerWithDefaultPrettyPrinter()
//            .writeValue(file, geoJson)
//    }

    fun exportGraph(
        graph: Graph,
        start: Point,
        points: List<Point>,
        file: File,
        edgeCostFunction: (Edge) -> Double? = { null },
    ) {
        val features = graph.edges.map { edge ->
            val coordinates = edge.geometry.coordinates.map { listOf(it.x, it.y) }
            val cost = edgeCostFunction(edge)

            val totalDistance = edge.segments!!.sumOf { it.distanceMeters }

            var gre = 0.0
            var wa = 0.0
            var tr = 0.0
            var no = 0.0
            var hi = 0.0
            var mo = 0.0
            var co = 0.0
            var gri = 0.0
            var pe = 0.0
            edge.segments.forEach { segment ->
                segment.features!!
                gre += segment.features.greenery * segment.distanceMeters
                wa += segment.features.water * segment.distanceMeters
                tr += segment.features.traffic * segment.distanceMeters
                no += segment.features.noise * segment.distanceMeters
                hi += segment.features.historicArchitecture * segment.distanceMeters
                mo += segment.features.modernArchitecture * segment.distanceMeters
                co += segment.features.commercialActivity * segment.distanceMeters
                gri += segment.features.grittiness * segment.distanceMeters
                pe += segment.features.pedestrianComfort * segment.distanceMeters
            }
            gre /= totalDistance
            wa /= totalDistance
            tr /= totalDistance
            no /= totalDistance
            hi /= totalDistance
            mo /= totalDistance
            co /= totalDistance
            gri /= totalDistance
            pe /= totalDistance

            mapOf(
                "type" to "Feature",

                "properties" to mapOf(
                    "wayOsmId" to edge.wayOsmId,
                    "distance" to edge.distance,
                    "walking time" to edge.walkingTime,
                    "cost" to cost,
//                    "greenery" to gre,
//                    "water" to wa,
//                    "traffic" to tr,
//                    "noise" to no,
//                    "historicArchitecture" to hi,
//                    "modernArchitecture" to mo,
//                    "commercialActivity" to co,
//                    "grittiness" to gri,
//                    "pedestrianComfort" to pe,

//                    "stroke" to "#383838",
                    "stroke" to Color.black.toHexString(),
                    "stroke-width" to 2,
                    "stroke-opacity" to 1.0
                ),

                "geometry" to mapOf(
                    "type" to "LineString",
                    "coordinates" to coordinates
                )
            )
        } + points.filter { it != start }.map {
            mapOf(
                "type" to "Feature",

                "properties" to mapOf(
                    "stroke" to Color.blue.toHexString(),
                    "stroke-width" to 2,
                    "stroke-opacity" to 1.0
                ),

                "geometry" to mapOf(
                    "type" to "Point",
                    "coordinates" to listOf(it.x, it.y)
                )
            )
        } + mapOf(
            "type" to "Feature",

            "properties" to mapOf(
                "color" to Color.red.toHexString(),
                "stroke" to Color.red.toHexString(),
                "stroke-width" to 3,
                "stroke-opacity" to 1.0
            ),

            "geometry" to mapOf(
                "type" to "Point",
                "coordinates" to listOf(start.x, start.y)
            )
        )

        val geoJson = mapOf(
            "type" to "FeatureCollection",
            "features" to features
        )

        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(file, geoJson)
    }

    fun exportData(data: OsmData, file: File) {
        val features = data.nodes.map { node -> jsonizeNode(node) } +
                data.ways.mapNotNull { way -> jsonizeWay(way, data.nodesMap) } +
                data.relations.flatMap { relation ->
                    relation.members.mapNotNull { member ->
                        val way = data.waysMap[member.wayId] ?: return@mapNotNull null
                        jsonizeWay(way, data.nodesMap, relation.tags)
                    }
                }

        val geoJson = mapOf(
            "type" to "FeatureCollection",
            "features" to features
        )

        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(file, geoJson)
    }

    private fun jsonizeNode(node: OsmNodeDto): Map<String, Any> {
        return mapOf(
            "type" to "Feature",

            "properties" to mapOf(
                "osmNodeId" to node.id,
                "tags" to node.tags,

                "stroke" to "#1ED439",
                "stroke-width" to 3,
                "stroke-opacity" to 1.0
            ),

            "geometry" to mapOf(
                "type" to "Point",
                "coordinates" to listOf(node.lon, node.lat)
            )
        )
    }

    private fun jsonizeWay(
        way: OsmWayDto,
        nodesMap: Map<Long, OsmNodeDto>,
        relationTags: Map<String, String>? = null,
    ): Map<String, Any>? {
        val coordinates = way.nodeIds
            .mapNotNull { nodesMap[it] }
            .map { node ->
                listOf(node.lon, node.lat)
            }

        if (coordinates.size < 2) {
            return null
        }

        val multipolygon = relationTags != null

        return mapOf(
            "type" to "Feature",

            "properties" to mapOf(
                "osmWayId" to way.id,
                "tags" to way.tags,
                "multipolygon" to multipolygon,
                "relationTags" to relationTags,

                "stroke" to if (multipolygon) "#881ED4" else "#1E52D4",
                "stroke-width" to 3,
                "stroke-opacity" to 1.0
            ),

            "geometry" to mapOf(
                "type" to "LineString",
                "coordinates" to coordinates
            )
        )
    }

    fun exportUrbanObjects(objects: List<UrbanObject>, file: File) {
        val features = objects.map { obj ->
            val coordinates = obj.geometry.coordinates.map { listOf(it.x, it.y) }

            mapOf(
                "type" to "Feature",

                "properties" to mapOf(
                    "osmElementType" to obj.osmElementType,
                    "osmId" to obj.osmId,
                    "tags" to obj.tags,
                    "urbanObjectType" to obj.urbanObjectType,

                    "stroke" to "#1E52D4",
                    "stroke-width" to 4,
                    "stroke-opacity" to 1.0
                ),

                "geometry" to mapOf(
                    "type" to "LineString",
                    "coordinates" to coordinates
                )
            )
        }

        val geoJson = mapOf(
            "type" to "FeatureCollection",
            "features" to features
        )

        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(file, geoJson)
    }

    fun exportLines(lines: List<LineString>, file: File) {
        val features = lines.map { line ->
            val coordinates = line.coordinates.map { listOf(it.x, it.y) }

            mapOf(
                "type" to "Feature",

                "properties" to mapOf(
                    "stroke" to "#1E52D4",
                    "stroke-width" to 4,
                    "stroke-opacity" to 1.0
                ),

                "geometry" to mapOf(
                    "type" to "LineString",
                    "coordinates" to coordinates
                )
            )
        }

        val geoJson = mapOf(
            "type" to "FeatureCollection",
            "features" to features
        )

        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(file, geoJson)
    }

    fun exportRoute(route: Route, file: File) {
        val features = route.paths
            .flatMap { path -> path.edges }
            .map { edge ->
                val coordinates = edge.geometry.coordinates.map { listOf(it.x, it.y) }

                mapOf(
                    "type" to "Feature",

                    "properties" to mapOf(
                        "stroke" to "#1E52D4",
                        "stroke-width" to 4,
                        "stroke-opacity" to 1.0
                    ),

                    "geometry" to mapOf(
                        "type" to "LineString",
                        "coordinates" to coordinates
                    )
                )
        } + route.paths.mapIndexed { index, path ->
            val coordinates = path.start.geometry.coordinates.flatMap { listOf(it.x, it.y) }

            mapOf(
                "type" to "Feature",

                "properties" to mapOf(
                    "index" to index,

                    "stroke" to "#D41E1E",
                    "stroke-width" to 4,
                    "stroke-opacity" to 1.0
                ),

                "geometry" to mapOf(
                    "type" to "Point",
                    "coordinates" to coordinates
                )
            )
        }

        val geoJson = mapOf(
            "type" to "FeatureCollection",
            "features" to features
        )

        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(file, geoJson)
    }

    private fun Color.toHexString(): String =
        "#" + Integer.toHexString(rgb).substring(2)
}