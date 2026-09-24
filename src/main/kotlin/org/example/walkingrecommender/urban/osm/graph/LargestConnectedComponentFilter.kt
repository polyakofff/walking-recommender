package org.example.walkingrecommender.urban.osm.graph

import org.example.walkingrecommender.urban.Graph
import org.springframework.stereotype.Component

@Component
class LargestConnectedComponentFilter {

    fun filter(graph: Graph): Graph {
        val dsu = DisjointSetUnion<Long>()

        for (node in graph.nodes.values) {
            dsu.add(node.nodeOsmId)
        }

        for (edge in graph.edges) {
            dsu.union(edge.nodeAOsmId, edge.nodeBOsmId)
        }

        val distanceByComponent = mutableMapOf<Long, Int>()
        for (edge in graph.edges) {
            val component = dsu.find(edge.nodeAOsmId)
            distanceByComponent.merge(component, edge.distance, Integer::sum)
        }

        val largestComponent = distanceByComponent.maxBy { it.value }.key

        val nodes = graph.nodes.filter { dsu.find(it.key) == largestComponent }
        val edges = graph.edges.filter { dsu.find(it.nodeAOsmId) == largestComponent }

        return Graph(
            nodes = nodes,
            edges = edges,
        )
    }
}