package org.example.walkingrecommender.core.routing

import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.model.externalId
import java.util.PriorityQueue

data class DijkstraResult(
    val costMap: Map<Long, Double>,
    val timeMap: Map<Long, Int>,
)

fun dijkstra(start: Node, graph: GraphUtils): DijkstraResult {
    val q = PriorityQueue<Pair<Node, Double>> { p1, p2 -> p1.second.compareTo(p2.second) }
    q.add(start to 0.0)

    val costs = mutableMapOf<Long, Double>()
    costs[start.externalId()] = 0.0

    val times = mutableMapOf<Long, Int>()
    times[start.externalId()] = 0

    while (q.isNotEmpty()) {
        val (node, c) = q.poll()

        if (costs[node.externalId()]!! < c) continue

        for (edge in graph.adjacent(node)) {
            val toId = if (edge.nodeAOsmId == node.externalId()) edge.nodeBOsmId else edge.nodeAOsmId
            val to = graph.node(toId)

            val newCost = costs[node.externalId()]!! + graph.cost(edge)
            val newTime = times[node.externalId()]!! + edge.walkingTime
            if (toId !in costs || newCost < costs[toId]!!) {
                costs[toId] = newCost
                times[toId] = newTime
                q.add(to to newCost)
            }
        }
    }

    return DijkstraResult(costs, times)
}

data class Path(
    val start: Node,
    val end: Node,
    val edges: List<Edge>,
)

fun aStar(start: Node, goal: Node, graph: GraphUtils): Path? {
    val q = PriorityQueue<Pair<Node, Double>> { p1, p2 -> p1.second.compareTo(p2.second) }
    q.add(start to 0.0)

    val costs = mutableMapOf<Long, Double>()
    costs[start.externalId()] = 0.0

    val cameBy = mutableMapOf<Long, Edge>()

    while (!q.isEmpty()) {
        val (node, _) = q.poll()

        if (node.externalId() == goal.externalId()) {
            return buildPath(start, goal, cameBy)
        }

        for (edge in graph.adjacent(node)) {
            val toId = if (edge.nodeAOsmId == node.externalId()) edge.nodeBOsmId else edge.nodeAOsmId
            val to = graph.node(toId)

            val newCost = costs[node.externalId()]!! + graph.cost(edge)
            if (toId !in costs || newCost < costs[toId]!!) {
                costs[toId] = newCost
                cameBy[toId] = edge
                val priority = newCost + graph.heuristic(to, goal)
                q.add(to to priority)
            }
        }
    }

    return null
}

fun buildPath(start: Node, goal: Node, cameBy: Map<Long, Edge>): Path {
    val edges = mutableListOf<Edge>()
    var nodeId = goal.externalId()
    while (cameBy[nodeId] != null) {
        val edge = cameBy[nodeId]!!
        edges += edge
        nodeId = if (edge.nodeAOsmId == nodeId) edge.nodeBOsmId else edge.nodeAOsmId
    }
    return Path(start, goal, edges.reversed())
}