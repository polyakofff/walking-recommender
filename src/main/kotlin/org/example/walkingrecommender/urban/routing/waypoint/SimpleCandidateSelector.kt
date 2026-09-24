package org.example.walkingrecommender.urban.routing.waypoint

import org.example.walkingrecommender.urban.Graph
import org.example.walkingrecommender.urban.Node
import org.example.walkingrecommender.urban.distance
import org.example.walkingrecommender.urban.externalId

class SimpleCandidateSelector(
    private val graph: Graph,
    private val startToNodeTime: Map<Long, Int>,
) : CandidateSelector {

    private val candidatesDistanceMeters = 100

    override fun selectCandidates(): List<Node> {
        val adjacency = mutableMapOf<Long, Int>()
        graph.edges.forEach { edge ->
            adjacency.merge(edge.nodeAOsmId, 1, Int::plus)
            adjacency.merge(edge.nodeBOsmId, 1, Int::plus)
        }

        val sortedCandidates = graph.nodes.values
            .filter { startToNodeTime[it.externalId()] != null }
            .sortedByDescending { adjacency[it.externalId()] }

        return selectDiverseCandidates(sortedCandidates)
    }

    private fun selectDiverseCandidates(candidates: List<Node>): List<Node> {
        val result = mutableListOf<Node>()

        for (candidate in candidates) {
            if (result.none { distance(it, candidate) <= candidatesDistanceMeters }) {
                result += candidate
            }
        }

        return result
    }
}