package org.example.walkingrecommender.urban.routing.route

import org.example.walkingrecommender.urban.Graph
import org.example.walkingrecommender.urban.Node
import org.example.walkingrecommender.urban.db.GraphRepository
import org.example.walkingrecommender.urban.distance
import org.example.walkingrecommender.urban.externalId
import org.example.walkingrecommender.urban.routing.CalculatedRoute
import org.example.walkingrecommender.urban.routing.GraphUtils
import org.example.walkingrecommender.urban.routing.Route
import org.example.walkingrecommender.urban.routing.ScenicCostFunction
import org.example.walkingrecommender.urban.routing.aStar
import org.example.walkingrecommender.urban.routing.dijkstra
import org.example.walkingrecommender.urban.routing.waypoint.WaypointGenerator
import org.example.walkingrecommender.urban.routing.waypoint.WaypointGeneratorFactory
import org.example.walkingrecommender.user.UserPreferences
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Component
import kotlin.math.max

data class ExtraData(
    val graph: Graph,
    val candidates: List<Node>,
)

@Component
class RouteGenerator(
    private val graphRepository: GraphRepository,
    private val waypointGeneratorFactory: WaypointGeneratorFactory,
    private val routeCalculator: RouteCalculator,
    private val routeScorer: RouteScorer,
    private val routeSelector: RouteSelector,
) {

    fun generate(
        startPoint: Point,
        goalPoint: Point,
        durationSeconds: Int,
        userPreferences: UserPreferences,
        scenicWeight: Double = 0.5,
    ): Pair<List<CalculatedRoute>, ExtraData> {
        val directDistance = distance(startPoint, goalPoint)
        val desiredDistance = distance(durationSeconds)
        val radius = max(directDistance * 1.5, desiredDistance)

        val graph = graphRepository.findSubgraph(startPoint, radius)
        println("nodes: ${graph.nodes.size}")
        println("edges: ${graph.edges.size}")

        val (start, _) = graphRepository.findNearestNode(startPoint)
            ?: throw IllegalStateException("start not found")

        val (goal, _) = graphRepository.findNearestNode(goalPoint)
            ?: throw IllegalStateException("goal not found")

        val costGraph = GraphUtils(
            graph,
            ScenicCostFunction(
                userPreferences = userPreferences,
                scenicWeight = scenicWeight,
            ),
        )

        val (_, startToNodeTime) = dijkstra(
            start,
            costGraph,
        )

        if (goal.externalId() !in startToNodeTime) {
            throw IllegalStateException("goal is not reachable from start")
        }

        val (_, goalToNodeTime) = dijkstra(
            goal,
            costGraph,
        )

        val waypointGenerator = waypointGeneratorFactory.create(
            graph = graph,
            start = start,
            goal = goal,
            startToNodeTime = startToNodeTime,
            goalToNodeTime = goalToNodeTime,
            desiredTime = durationSeconds,
        )

        val candidates = waypointGenerator.getCandidates()

        val routes = if (start.externalId() != goal.externalId()) { // A != B => path, 0-1 waypoints
            val routes0 = get0WaypointRoutes(start, goal, costGraph)

            val routes1 = get1WaypointRoutes(start, goal, costGraph, waypointGenerator)

            routes0 + routes1

        } else { // A = B => circular, 2-3 waypoints
            val routes2 = get2WaypointRoutes(start, goal, costGraph, waypointGenerator)

            // TODO: triples

            routes2
        }

        println("routes: ${routes.size}")

        val calculatedRoutes = routes.map { route ->
            routeCalculator.calculate(route, userPreferences, durationSeconds)
        }

        val rankedRoutes = calculatedRoutes.sortedByDescending { route ->
            routeScorer.score(route)
        }

        val finalRoutes = routeSelector.select(rankedRoutes)
        println("finalRoutes: ${finalRoutes.size}")

        return finalRoutes to ExtraData(graph, candidates)
    }

    private fun get0WaypointRoutes(
        start: Node,
        goal: Node,
        graph: GraphUtils,
    ): List<Route> {
        val path = aStar(
            start,
            goal,
            graph,
        )!!

        return listOf(
            Route(
                start = start,
                goal = goal,
                paths = listOf(path),
            )
        )
    }

    private fun get1WaypointRoutes(
        start: Node,
        goal: Node,
        graph: GraphUtils,
        waypointGenerator: WaypointGenerator,
    ): List<Route> {
        return waypointGenerator.generateWaypoints().mapNotNull { node ->
            val path1 = aStar(
                start,
                node,
                graph,
            ) ?: return@mapNotNull null
            val path2 = aStar(
                node,
                goal,
                graph,
            ) ?: return@mapNotNull null

            Route(
                start = start,
                goal = goal,
                paths = listOf(path1, path2),
            )
        }
    }

    private fun get2WaypointRoutes(
        start: Node,
        goal: Node,
        graph: GraphUtils,
        waypointGenerator: WaypointGenerator,
    ): List<Route> {
        return waypointGenerator.generateWaypointPairs().mapNotNull { (n1, n2) ->
            val path1 = aStar(
                start,
                n1,
                graph,
            ) ?: return@mapNotNull null
            val path2 = aStar(
                n1,
                n2,
                graph,
            ) ?: return@mapNotNull null
            val path3 = aStar(
                n2,
                goal,
                graph,
            ) ?: return@mapNotNull null

            Route(
                start = start,
                goal = goal,
                paths = listOf(path1, path2, path3),
            )
        }
    }
}