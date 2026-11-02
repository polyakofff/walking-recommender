package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.MemoryUtils
import org.example.walkingrecommender.properties.WalkingRecommenderProperties
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.db.GraphRepository
import org.example.walkingrecommender.core.distance
import org.example.walkingrecommender.core.model.externalId
import org.example.walkingrecommender.core.routing.*
import org.example.walkingrecommender.core.routing.waypoint.WaypointGenerator
import org.example.walkingrecommender.core.routing.waypoint.WaypointGeneratorFactory
import org.example.walkingrecommender.user.Preferences
import org.locationtech.jts.geom.Point
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.math.max

data class WaypointRoute(
    val route: Route,
    val waypoints: List<Node>,
)

data class GenerationResult(
    val routes: List<ScoredRoute>,
    val data: ExtraData,
)

data class ExtraData(
    val graph: Graph,
    val candidates: List<Node>,
)

@Component
class RouteGenerator(
    private val properties: WalkingRecommenderProperties,
    private val graphRepository: GraphRepository,
    private val waypointGeneratorFactory: WaypointGeneratorFactory,
    private val routeCalculator: RouteCalculator,
    private val routeScorer: RouteScorer,
    private val routeSelector: RouteSelector,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generate(
        startPoint: Point,
        goalPoint: Point,
        durationSeconds: Int,
        preferences: Preferences,
        scenicWeight: Double,
        waypointUse: BooleanArray,
    ): GenerationResult {
        val directDistance = distance(startPoint, goalPoint)
        val desiredDistance = distance(durationSeconds)
        val radius = max(
            directDistance * properties.generatorDirectDistanceFactor,
            desiredDistance
        )

        val before = MemoryUtils.heapUsedMb()

        val graph = try {
            graphRepository.findSubgraph(startPoint, radius)
        } catch (e: Exception) {
            logger.error("Unable to load graph", e)
            return GenerationResult(
                emptyList(),
                ExtraData(
                    Graph(
                        emptyMap(),
                        emptyList()
                    ),
                    emptyList()
                )
            )
        }

        val after = MemoryUtils.heapUsedMb()

        logger.info(
            "Subgraph loaded: edges={}, nodes={}, heapBefore={} MB, heapAfter={} MB, heapDelta={} MB, heapMax={} MB",
            graph.edges.size,
            graph.nodes.size,
            before,
            after,
            after - before,
            MemoryUtils.heapMaxMb(),
        )

        val (start, _) = graphRepository.findNearestNode(startPoint)
            ?: error("start not found")

        val (goal, _) = graphRepository.findNearestNode(goalPoint)
            ?: error("goal not found")

        val costGraph = GraphUtils(
            graph,
            ScenicCostFunction(
                preferences = preferences,
                scenicWeight = scenicWeight,
            ),
        )

        val (_, startToNodeTime) = dijkstra(
            start,
            costGraph,
        )

        if (goal.externalId() !in startToNodeTime) {
            error("goal is not reachable from start")
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

        val routes = mutableListOf<WaypointRoute>()
        if (waypointUse[0]) {
            routes += get0WaypointRoutes(start, goal, costGraph)
        }
        if (waypointUse[1]) {
            routes += get1WaypointRoutes(start, goal, costGraph, waypointGenerator)
        }
        if (waypointUse[2]) {
            routes += get2WaypointRoutes(start, goal, costGraph, waypointGenerator)
        }

        logger.info("routes: ${routes.size}")

        val calculatedRoutes = routes.map { route ->
            routeCalculator.calculate(route.route, preferences, durationSeconds)
        }

        val rankedRoutes = calculatedRoutes
            .map { route -> routeScorer.score(route) }
            .sortedByDescending { it.score }

        val finalRoutes = routeSelector.select(rankedRoutes)
        logger.info("finalRoutes: ${finalRoutes.size}")

        return GenerationResult(
            routes = finalRoutes,
            data = ExtraData(
                graph = graph,
                candidates = candidates
            )
        )
    }

    private fun get0WaypointRoutes(
        start: Node,
        goal: Node,
        graph: GraphUtils,
    ): List<WaypointRoute> {
        val path = aStar(
            start,
            goal,
            graph,
        )!!

        return listOf(
            WaypointRoute(
                route = Route(
                    start = start,
                    goal = goal,
                    paths = listOf(path),
                ),
                waypoints = listOf(),
            )
        )
    }

    private fun get1WaypointRoutes(
        start: Node,
        goal: Node,
        graph: GraphUtils,
        waypointGenerator: WaypointGenerator,
    ): List<WaypointRoute> {
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

            WaypointRoute(
                route = Route(
                    start = start,
                    goal = goal,
                    paths = listOf(path1, path2),
                ),
                waypoints = listOf(node),
            )
        }
    }

    private fun get2WaypointRoutes(
        start: Node,
        goal: Node,
        graph: GraphUtils,
        waypointGenerator: WaypointGenerator,
    ): List<WaypointRoute> {
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

            WaypointRoute(
                route = Route(
                    start = start,
                    goal = goal,
                    paths = listOf(path1, path2, path3),
                ),
                waypoints = listOf(n1, n2),
            )
        }
    }
}