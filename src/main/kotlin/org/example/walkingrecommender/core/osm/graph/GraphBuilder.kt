package org.example.walkingrecommender.core.osm.graph

import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.distance
import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.time
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Component
import kotlin.collections.plusAssign
import kotlin.math.roundToInt

@Component
class GraphBuilder(private val geometryFactory: GeometryFactory) {

    fun build(data: OsmData): Graph {
        val nodeUsage = mutableMapOf<Long, Int>()
        for (way in data.ways) {
            for (nodeId in way.nodeIds) {
                nodeUsage.merge(nodeId, 1, Int::plus)
            }
        }

        val edges = mutableListOf<Edge>()
        for (way in data.ways) {
            var startIndex = 0
            var distance = 0.0
            for (i in 1..<way.nodeIds.size) {
                val prevNodeId = way.nodeIds[i - 1]
                val nodeId = way.nodeIds[i]
                distance += distance(
                    data.nodesMap[prevNodeId]!!,
                    data.nodesMap[nodeId]!!,
                )
                if (nodeUsage[nodeId]!! > 1 || i == way.nodeIds.size - 1) {
                    edges += Edge(
                        id = -1,
                        nodeAOsmId = way.nodeIds[startIndex],
                        nodeBOsmId = nodeId,
                        wayOsmId = way.id,
                        geometry = geometryFactory.createLineString(
                            way.nodeIds.subList(startIndex, i + 1).map {
                                Coordinate(
                                    data.nodesMap[it]!!.lon,
                                    data.nodesMap[it]!!.lat
                                )
                            }.toTypedArray()
                        ),
                        highway = way.tags["highway"]!!,
                        surface = way.tags["surface"],
                        name = way.tags["name"],
                        lanes = way.tags["lanes"]?.toIntOrNull(),
                        lit = way.tags["lit"] == "yes",
                        distance = distance.roundToInt(),
                        walkingTime = time(distance).roundToInt(),
                    )
                    startIndex = i
                    distance = 0.0
                }
            }
        }

        val nodes = edges
            .flatMap {
                listOf(
                    it.nodeAOsmId,
                    it.nodeBOsmId
                )
            }
            .distinct()
            .associateWith { osmId ->
                Node(
                    id = -1,
                    nodeOsmId = osmId,
                    geometry = geometryFactory.createPoint(
                        Coordinate(
                            data.nodesMap[osmId]!!.lon,
                            data.nodesMap[osmId]!!.lat
                        )
                    )
                )
            }

        return Graph(
            nodes = nodes,
            edges = edges,
        )
    }
}