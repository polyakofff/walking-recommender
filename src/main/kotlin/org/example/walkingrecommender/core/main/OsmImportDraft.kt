package org.example.walkingrecommender.core.main

import org.example.walkingrecommender.core.osm.graph.GraphBuilder
import org.example.walkingrecommender.core.osm.graph.LargestConnectedComponentFilter
import org.example.walkingrecommender.core.osm.OsmParser
import org.example.walkingrecommender.core.osm.extractor.ForestExtractor
import org.example.walkingrecommender.core.osm.extractor.GeometryBuilder
import org.example.walkingrecommender.core.osm.filter.ForestOsmFilter
import org.example.walkingrecommender.core.osm.filter.OsmDataFilter
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import java.io.File
import java.nio.file.Paths
import kotlin.collections.contains
import kotlin.io.path.createDirectories

object OsmImportDraft {

    @JvmStatic
    fun main(args: Array<String>) {
        val geometryFactory = GeometryFactory(
            PrecisionModel(),
            4326
        )

        val geometryBuilder = GeometryBuilder(geometryFactory)

        val filter = OsmDataFilter()

        val exporter = GeoJsonExporter()

        val data = OsmParser().parse(File("data/osm/odintsovo.osm"))
//        val data = OsmParser().parse(File("data/osm/lazutinka.osm"))

        val walkingGraphData = filter.filterByWalkingGraph(data)

        println("nodes: ${data.nodesMap.size}")
        println("all ways: ${data.ways.size}")
        println("graph ways: ${walkingGraphData.ways.size}")

//        GeoJsonExporter().export(
//            data = data,
//            isInGraph = { way -> filter.isInGraph(way) },
//            isWalkable = { way -> filter.isWalkable(way) },
//            isDrivable = { way -> filter.isDrivable(way) },
//            isStreet = { way -> filter.isStreet(way) },
//            file = File("data/ways.geojson")
//        )




        var graph = GraphBuilder(geometryFactory).build(walkingGraphData)
        graph = LargestConnectedComponentFilter().filter(graph)
        println("edges: ${graph.edges.size}")
//        GeoJsonExporter().exportGraph(graph, File("data/geojson/graph.geojson"))






//        val natural = filter.filterByNatural(data)
//
//        val naturalWaysGrouped = natural.ways.groupBy { it.tags["natural"] }
//        val naturalRelationsGrouped = natural.relations.groupBy { it.tags["natural"] }
//
//        val naturalGrouped = (naturalWaysGrouped.keys union naturalRelationsGrouped.keys).associateWith { key ->
//            OsmData(
//                nodesMap = data.nodesMap,
//                waysMap = data.waysMap,
//                nodes = listOf(),
//                ways = naturalWaysGrouped[key] ?: emptyList(),
//                relations = naturalRelationsGrouped[key] ?: emptyList(),
//            )
//        }
//
//        val folder = Paths.get("data/geojson/natural")
//        folder.createDirectories()
//
//        naturalGrouped.forEach { (key, data) ->
//            exporter.exportData(
//                data = data,
//                file = File("data/geojson/natural/$key.geojson")
//            )
//        }




//        val leisure = filter.filterByLeisure(data)
//
//        val waysGrouped = leisure.ways.groupBy { it.tags["leisure"] }
//        val relationsGrouped = leisure.relations.groupBy { it.tags["leisure"] }
//
//        val grouped = (waysGrouped.keys union relationsGrouped.keys).associateWith { key ->
//            OsmData(
//                nodesMap = data.nodesMap,
//                waysMap = data.waysMap,
//                ways = waysGrouped[key] ?: emptyList(),
//                relations = relationsGrouped[key] ?: emptyList(),
//            )
//        }
//
//        val exporter = GeoJsonExporter()
//        grouped.forEach { (key, data) ->
//            exporter.exportData(
//                data = data,
//                file = File("data/geojson/$key.geojson")
//            )
//        }




//        val landuse = filter.filterByLanduse(data)
//
//        val landuseWaysGrouped = landuse.ways.groupBy { it.tags["landuse"] }
//        val landuseRelationsGrouped = landuse.relations.groupBy { it.tags["landuse"] }
//
//        val grouped = (landuseWaysGrouped.keys union landuseRelationsGrouped.keys).associateWith { key ->
//            OsmData(
//                nodesMap = data.nodesMap,
//                waysMap = data.waysMap,
//                nodes = data.nodes,
//                ways = landuseWaysGrouped[key] ?: emptyList(),
//                relations = landuseRelationsGrouped[key] ?: emptyList(),
//            )
//        }
//
//        grouped.forEach { (key, data) ->
//            exporter.exportData(
//                data = data,
//                file = File("data/geojson/landuse/$key.geojson")
//            )
//        }




        // TODO: building
        // TODO: place
        // TODO: amenity
        // TODO: waterway
        // TODO: aeroway
        // TODO: historic

//        val tag = "building"
//        val byTag = filter.filterByTag(data, tag)
//
//        val nodesGrouped = byTag.nodes.groupBy { it.tags[tag] }
//        val waysGrouped = byTag.ways.groupBy { it.tags[tag] }
//        val relationsGrouped = byTag.relations.groupBy { it.tags[tag] }
//
//        val grouped = (waysGrouped.keys union relationsGrouped.keys).associateWith { key ->
//            OsmData(
//                nodesMap = data.nodesMap,
//                waysMap = data.waysMap,
//                nodes = nodesGrouped[key] ?: emptyList(),
//                ways = waysGrouped[key] ?: emptyList(),
//                relations = relationsGrouped[key] ?: emptyList(),
//            )
//        }
//
//        val folder = Paths.get("data/geojson/$tag")
//        folder.createDirectories()
//
//        grouped.forEach { (key, data) ->
//            exporter.exportData(
//                data = data,
//                file = File(folder.pathString + "/$key.geojson")
//            )
//        }

        val drivable = filter.filterByDrivable(data)
        println("drivable: ${drivable.ways.size}")
        exporter.exportData(
            data = drivable,
            file = File("data/geojson/roads.geojson"),
        )

//        val otherWays = data.ways.filter { notInFilter(it.tags) }
//        val otherRelations = data.relations.filter { notInFilter(it.tags) }
//
//        println(otherWays.size)
//        println(otherRelations.size)
//
//        println(otherWays.joinToString { it.id.toString() })
//        println(otherRelations.joinToString { it.id.toString() })


        val extractor = ForestExtractor(geometryBuilder, ForestOsmFilter())
        val forests = extractor.extract(data)
//        println(forests.size)
//        println("Forests: $forests")

        val objectFolder = Paths.get("data/geojson/object")
        objectFolder.createDirectories()
        exporter.exportUrbanObjects(
            objects = forests,
            file = File("data/geojson/object/forests.geojson")
        )

    }

    private fun notInFilter(tags: Map<String, String>): Boolean {
        if (tags.isEmpty()) {
            return false
        }
        return "highway" !in tags &&
                "area:highway" !in tags &&
                "natural" !in tags &&
                "leisure" !in tags &&
                "landuse" !in tags &&
                "building" !in tags &&
                "building:part" !in tags &&
                "place" !in tags &&
                "amenity" !in tags &&
                "power" !in tags &&
                "railway" !in tags &&
                "barrier" !in tags &&
                "waterway" !in tags &&
                "aeroway" !in tags &&
                "boundary" !in tags &&
                "man_made" !in tags
    }
}