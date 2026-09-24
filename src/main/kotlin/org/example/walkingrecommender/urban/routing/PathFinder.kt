package org.example.walkingrecommender.urban.routing

import org.example.walkingrecommender.urban.Graph
import org.example.walkingrecommender.urban.db.GraphRepository
import org.example.walkingrecommender.urban.distance
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Component

data class LinesPath(
    val lines: List<LineString>,
)

@Component
class PathFinder(
    private val graphRepository: GraphRepository,
) {
    private val defaultRadiusMeters = 10_000.0
    private var graph: Graph? = null

    fun graph(): Graph? = graph

    fun find(
        from: Point,
        to: Point,
        costFunction: EdgeCostFunction,
    ): LinesPath? {
        val (fromNode, _) = graphRepository.findNearestNode(from)
            ?: throw IllegalStateException("fromNode not found")

        println("fromNode: $fromNode")

        val (toNode, _) = graphRepository.findNearestNode(to)
            ?: throw IllegalStateException("toNode not found")

        println("toNode: $toNode")

        val directDistance = distance(from, to)
        println("directDistance: $directDistance")

        val radius = maxOf(defaultRadiusMeters, directDistance * 1.5)

        graph = graphRepository.findSubgraph(from, radius)

        if (graph!!.edges.isEmpty()) {
            throw IllegalStateException("No graph at point $from within radius $radius")
        }

        val g = GraphUtils(graph!!, costFunction)

        val path = aStar(
            fromNode,
            toNode,
            g,
        )?.let { path ->
            LinesPath(
                path.edges.map { edge -> edge.geometry }
            )
        }
        if (path != null) {
            println("Path found: $path")
        } else {
            println("Cannot find path")
        }
        return path
    }
}