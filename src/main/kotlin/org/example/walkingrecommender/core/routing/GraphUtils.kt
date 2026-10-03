package org.example.walkingrecommender.core.routing

import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.distance
import org.example.walkingrecommender.core.model.externalId
import org.example.walkingrecommender.core.time

class GraphUtils(
    private val graph: Graph,
    private val costFunction: EdgeCostFunction
) {
    private val adjacentLists = mutableMapOf<Long, MutableList<Edge>>()

    init {
        graph.edges.forEach { edge ->
            adjacentLists.getOrPut(edge.nodeAOsmId) { mutableListOf() }.add(edge)
            adjacentLists.getOrPut(edge.nodeBOsmId) { mutableListOf() }.add(edge)
        }
    }

    fun node(id: Long): Node {
        return graph.nodes[id]!!
    }

    fun adjacent(node: Node): List<Edge> {
        return adjacentLists[node.externalId()].orEmpty()
    }

    fun cost(edge: Edge): Double {
        return costFunction.cost(edge)
    }

    fun heuristic(node: Node, goal: Node): Double {
        val distance = distance(
            node,
            goal
        )
        return time(distance) * costFunction.minCostMultiplier
    }
}