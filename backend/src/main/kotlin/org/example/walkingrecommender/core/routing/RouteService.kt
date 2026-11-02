package org.example.walkingrecommender.core.routing

import org.example.walkingrecommender.properties.WalkingRecommenderProperties
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.routing.route.RouteCalculator
import org.example.walkingrecommender.core.routing.route.RouteGenerator
import org.example.walkingrecommender.core.routing.route.RouteScorer
import org.example.walkingrecommender.user.Preferences
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Service

@Service
class RouteService(
    private val geometryFactory: GeometryFactory,
    private val routeGenerator: RouteGenerator,
    private val routeCalculator: RouteCalculator,
    private val routeScorer: RouteScorer,
    private val properties: WalkingRecommenderProperties,
) {

    val preferences0 = Preferences()

    fun algorithmVersion() = "1.0"

    fun getBestNRoutes(
        startLon: Double,
        startLat: Double,
        endLon: Double,
        endLat: Double,
        durationSeconds: Int,
        preferences: Preferences,
        n: Int,
    ): Pair<List<ScoredRoute>, Graph> {

        val startPoint = geometryFactory.createPoint(
            Coordinate(startLon, startLat),
        )
        val goalPoint = geometryFactory.createPoint(
            Coordinate(endLon, endLat),
        )

        val cyclic = startLon == endLon && startLat == endLat

        val waypointUse = booleanArrayOf(
            !cyclic,
            !cyclic,
            cyclic,
        )

        val (routes, data) = routeGenerator.generate(
            startPoint = startPoint,
            goalPoint = goalPoint,
            durationSeconds = durationSeconds,
            preferences = preferences,
            scenicWeight = properties.scenicWeight,
            waypointUse = waypointUse,
        )

        return routes.take(n) to data.graph
    }

    fun getPairedFastestRoute(route: ScoredRoute, graph: Graph): ScoredRoute {
        val timeGraph = GraphUtils(
            graph,
            FastestCostFunction(),
        )

        val paths = route.paths()
            .map { path ->
                aStar(
                    path.start,
                    path.end,
                    timeGraph
                ) ?: error("Path not found for start: ${path.start}, end: ${path.end}")
            }

        return Route(
            route.start(),
            route.goal(),
            paths
        ).let {
            routeCalculator.calculate(
                it,
                preferences0,
                route.requestedDuration()
            )
        }.let {
            routeScorer.score(it)
        }
    }
}