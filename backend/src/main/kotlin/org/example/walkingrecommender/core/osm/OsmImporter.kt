package org.example.walkingrecommender.core.osm

import org.example.walkingrecommender.MemoryUtils
import org.example.walkingrecommender.core.db.GraphRepository
import org.example.walkingrecommender.core.db.UrbanObjectRepository
import org.example.walkingrecommender.core.osm.extractor.UrbanObjectExtractor
import org.example.walkingrecommender.core.osm.filter.WalkableOsmFilter
import org.example.walkingrecommender.core.osm.graph.GraphBuilder
import org.example.walkingrecommender.core.osm.graph.LargestConnectedComponentFilter
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(javaClass)
    private val fileName = "../data/osm/moscow"

    fun importAll() {
        val data = osmParser.parse(File(fileName))

        printStats(data)
        printMemory()

        importGraph(data)
        importUrbanObjects(data)
    }

    fun importGraph(data: OsmData) {
        val graphData = walkableOsmFilter.filterByWalkable(data)

        logger.info("nodes: ${graphData.nodesMap.size}")
        logger.info("walkable ways: ${graphData.ways.size}")

        var graph = graphBuilder.build(graphData)

        graph = largestConnectedComponentFilter.filter(graph)

        printMemory()

        logger.info("final graph nodes: ${graph.nodes.size}")
        logger.info("final graph edges: ${graph.edges.size}")

        graphRepository.save(graph)

        printMemory()
    }

    fun importUrbanObjects(data: OsmData) {
        for (extractor in urbanObjectExtractors) {
            logger.info("Extractor: ${extractor.getUrbanObjectType()}")

            val objects = extractor.extract(data)

            logger.info("Objects: ${objects.size}")

            urbanObjectRepository.save(objects)
        }

        printMemory()
    }

    private fun printStats(data: OsmData) {
        logger.info("nodes: ${data.nodes.size}, ways: ${data.ways.size}, relations: ${data.relations.size}")
    }

    private fun printMemory() {
        logger.info(
            "Used memory: %.2f GB".format(
                MemoryUtils.heapUsedMb() / 1024.0
            )
        )
    }
}