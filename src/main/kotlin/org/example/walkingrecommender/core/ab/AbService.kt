package org.example.walkingrecommender.core.ab

import org.example.walkingrecommender.api.AbRequest
import org.example.walkingrecommender.api.Choice
import org.example.walkingrecommender.api.VoteRequest
import org.example.walkingrecommender.core.routing.CalculatedRoute
import org.example.walkingrecommender.core.routing.FastestCostFunction
import org.example.walkingrecommender.core.routing.GraphUtils
import org.example.walkingrecommender.core.routing.Route
import org.example.walkingrecommender.core.routing.aStar
import org.example.walkingrecommender.core.routing.route.RouteCalculator
import org.example.walkingrecommender.core.routing.route.RouteGenerator
import org.example.walkingrecommender.user.UserPreferences
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

data class Comparison(
    val id: String,
    val routeA: CalculatedRoute,
    val routeB: CalculatedRoute,
)

@Service
class AbService(
    private val geometryFactory: GeometryFactory,
    private val routeGenerator: RouteGenerator,
    private val routeCalculator: RouteCalculator,
) {

    private val prefs0 = UserPreferences()

    private val labels = mutableMapOf<String, String>()

    fun getComparisons(request: AbRequest): List<Comparison> {

        val startPoint = geometryFactory.createPoint(
            Coordinate(request.start.longitude, request.start.latitude)
        )
        val endPoint = geometryFactory.createPoint(
            Coordinate(request.end.longitude, request.end.latitude)
        )

        val prefs = UserPreferences(
            greenery = 0.3,
            water = 0.5,
            traffic = -0.5,
            noise = -0.3,
            historicArchitecture = 0.0,
            modernArchitecture = 0.5,
            commercialActivity = 0.0,
            grittiness = -1.0,
            pedestrianComfort = 1.0
        )
        // TODO: get prefs

        val (routes, data) = routeGenerator.generate(
            startPoint = startPoint,
            goalPoint = endPoint,
            durationSeconds = request.durationMinutes * 60,
            userPreferences = prefs,
        )

        val timeGraph = GraphUtils(
            data.graph,
            FastestCostFunction()
        )

        val comparisons = routes
            .take(request.comparisonCount)
            .map { route ->
                val paired = getPairedRoute(route, timeGraph, request.durationMinutes)

                val id = generateId()

                val (routeA, routeB) = if (Random.nextBoolean()) {
                    labels[id] = "B"
                    paired to route
                } else {
                    labels[id] = "A"
                    route to paired
                }

                Comparison(
                    id = id,
                    routeA = routeA,
                    routeB = routeB,
                )
            }

        return comparisons
    }

    private fun getPairedRoute(
        route: CalculatedRoute,
        graph: GraphUtils,
        durationMinutes: Int,
    ): CalculatedRoute {
        val paths = route.route.paths
            .map { path ->
                aStar(
                    path.start,
                    path.end,
                    graph
                ) ?: error("Path not found for start: ${path.start}, end: ${path.end}")
            }

        val paired = Route(
            route.route.start,
            route.route.goal,
            paths
        )

        return routeCalculator.calculate(
            paired,
            prefs0,
            durationMinutes * 60
        )
    }

    private fun generateId(): String {
        return UUID.randomUUID().toString()
    }

    fun vote(request: VoteRequest) {
        val label = labels[request.comparisonId]
            ?: throw IllegalStateException("Unknown comparison [${request.comparisonId}]")

        when {
            request.choice == Choice.EQUAL -> {
                println("Equal")
            }
            request.choice == Choice.NONE -> {
                println("None")
            }
            request.choice.name == label -> {
                println("Right")
            }
            request.choice.name != label -> {
                println("Wrong")
            }
        }
    }
}