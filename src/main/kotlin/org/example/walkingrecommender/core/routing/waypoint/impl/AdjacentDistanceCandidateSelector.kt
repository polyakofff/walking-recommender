package org.example.walkingrecommender.core.routing.waypoint.impl

import org.example.walkingrecommender.core.distance
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.model.externalId
import org.example.walkingrecommender.core.routing.waypoint.CandidateSelector

class AdjacentDistanceCandidateSelector(
    private val graph: Graph,
    private val startToNodeTime: Map<Long, Int>,
    private val candidateDistanceMeters: Int,
) : CandidateSelector {

    override fun selectCandidates(): List<Node> {
        val adjacentDistance = mutableMapOf<Long, Int>()
        graph.edges.forEach { edge ->
            adjacentDistance.merge(edge.nodeAOsmId, edge.distance, Int::plus)
            adjacentDistance.merge(edge.nodeBOsmId, edge.distance, Int::plus)
        }

        val sortedCandidates = graph.nodes.values
            .filter { startToNodeTime[it.externalId()] != null }
            .sortedByDescending { adjacentDistance[it.externalId()] }

        return selectDiverseCandidates(sortedCandidates)
    }

    private fun selectDiverseCandidates(candidates: List<Node>): List<Node> {
        val result = mutableListOf<Node>()

        for (candidate in candidates) {
            if (result.none { distance(it, candidate) <= candidateDistanceMeters }) {
                result += candidate
            }
        }

        return result
    }

}