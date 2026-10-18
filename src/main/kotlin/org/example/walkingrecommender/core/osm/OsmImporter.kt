package org.example.walkingrecommender.core.osm

import org.example.walkingrecommender.core.db.GraphRepository
import org.example.walkingrecommender.core.db.UrbanObjectRepository
import org.example.walkingrecommender.core.osm.extractor.UrbanObjectExtractor
import org.example.walkingrecommender.core.osm.filter.WalkableOsmFilter
import org.example.walkingrecommender.core.osm.graph.GraphBuilder
import org.example.walkingrecommender.core.osm.graph.LargestConnectedComponentFilter
import org.springframework.stereotype.Component
import java.io.File

@Component
class OsmImporter(
    private val osmParser: OsmParser,
    private val walkableOsmFilter: WalkableOsmFilter,
    private val graphBuilder: GraphBuilder,
    private val largestConnectedComponentFilter: LargestConnectedComponentFilter,
    private val urbanObjectExtractors: List<UrbanObjectExtractor>,
    private val graphRepository: GraphRepository,
    private val urbanObjectRepository: UrbanObjectRepository,
) {

    private val fileName = "data/osm/moscow"

    fun importAll() {
        val data = osmParser.parse(File(fileName))

        printStats(data)
        printMemory()

        importGraph(data)
        importUrbanObjects(data)
    }

    fun importGraph(data: OsmData) {
        val graphData = walkableOsmFilter.filterByWalkable(data)

        println("nodes: ${graphData.nodesMap.size}")
        println("walkable ways: ${graphData.ways.size}")

        var graph = graphBuilder.build(graphData)

        graph = largestConnectedComponentFilter.filter(graph)

        printMemory()

        println("final graph nodes: ${graph.nodes.size}")
        println("final graph edges: ${graph.edges.size}")

        graphRepository.save(graph)

        printMemory()
    }

    fun importUrbanObjects(data: OsmData) {
        for (extractor in urbanObjectExtractors) {
            println("Extractor: ${extractor.getUrbanObjectType()}")

            val objects = extractor.extract(data)

            println("Objects: ${objects.size}")

            urbanObjectRepository.save(objects)
        }

        printMemory()
    }

    private fun printStats(data: OsmData) {
        println("nodes: ${data.nodes.size}")
        println("ways: ${data.ways.size}")
        println("relations: ${data.relations.size}")
    }

    private fun printMemory() {
        val runtime = Runtime.getRuntime()

        val used = runtime.totalMemory() - runtime.freeMemory()

        println(
            "Used memory: %.2f GB".format(
                used / 1024.0 / 1024 / 1024
            )
        )
    }
}