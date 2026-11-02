package org.example.walkingrecommender.core.routing.waypoint.impl

import org.example.walkingrecommender.core.distance
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.model.externalId
import org.example.walkingrecommender.core.routing.waypoint.CandidateSelector

class AdjacentCountCandidateSelector(
    private val graph: Graph,
    private val startToNodeTime: Map<Long, Int>,
) : CandidateSelector {

    private val distanceMeters = 150

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
            if (result.none { distance(it, candidate) <= distanceMeters }) {
                result += candidate
            }
        }

        return result
    }
}