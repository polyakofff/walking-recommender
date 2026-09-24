package org.example.walkingrecommender.urban.routing.route

import org.example.walkingrecommender.urban.routing.CalculatedRoute
import org.springframework.stereotype.Component
import kotlin.math.min

@Component
class DiverseRouteSelector : RouteSelector {

    private val thresholdRatio = 0.9

    override fun select(routes: List<CalculatedRoute>): List<CalculatedRoute> {
        val selected = mutableListOf<CalculatedRoute>()

        for (route in routes) {
            val similar = selected.any { selectedRoute ->
                areSimilar(route, selectedRoute)
            }
            if (!similar) {
                selected += route
            }
        }
        return selected
    }

    private fun areSimilar(route1: CalculatedRoute, route2: CalculatedRoute): Boolean {
        val route1EdgeToDistanceMap = route1.route
            .paths
            .flatMap { path -> path.edges }
            .groupBy { edge -> edge.id }
            .mapValues { (_, edges) -> edges.sumOf { it.distance } }

        val route2EdgeToDistanceMap = route2.route
            .paths
            .flatMap { path -> path.edges }
            .groupBy { edge -> edge.id }
            .mapValues { (_, edges) -> edges.sumOf { it.distance } }

        val intersectionDistance = route1EdgeToDistanceMap.entries.sumOf { (edgeId, distance) ->
            val route2Distance = route2EdgeToDistanceMap.getOrDefault(edgeId, 0)

            min(distance, route2Distance)
        }

        val route1IntersectionRatio = intersectionDistance.toDouble() / route1.distance
        val route2IntersectionRatio = intersectionDistance.toDouble() / route2.distance

        return route1IntersectionRatio >= thresholdRatio && route2IntersectionRatio >= thresholdRatio
    }
}