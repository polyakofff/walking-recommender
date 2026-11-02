package org.example.walkingrecommender.core.routing.route.impl

import org.example.walkingrecommender.properties.WalkingRecommenderProperties
import org.example.walkingrecommender.core.routing.ScoredRoute
import org.example.walkingrecommender.core.routing.route.RouteSelector
import org.springframework.stereotype.Component
import kotlin.math.min

@Component
class DifferentRouteSelector(
    properties: WalkingRecommenderProperties,
) : RouteSelector {

    private val thresholdRatio = properties.routeDifferenceThresholdRatio

    override fun select(routes: List<ScoredRoute>): List<ScoredRoute> {
        val selected = mutableListOf<ScoredRoute>()

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

    private fun areSimilar(route1: ScoredRoute, route2: ScoredRoute): Boolean {
        val route1EdgeToDistanceMap = route1
            .edges()
            .groupBy { edge -> edge.id }
            .mapValues { (_, edges) -> edges.sumOf { it.distance } }

        val route2EdgeToDistanceMap = route2
            .edges()
            .groupBy { edge -> edge.id }
            .mapValues { (_, edges) -> edges.sumOf { it.distance } }

        val intersectionDistance = route1EdgeToDistanceMap.entries.sumOf { (edgeId, distance) ->
            val route2Distance = route2EdgeToDistanceMap.getOrDefault(edgeId, 0)
            min(distance, route2Distance)
        }

        val route1IntersectionRatio = intersectionDistance.toDouble() / route1.distance()
        val route2IntersectionRatio = intersectionDistance.toDouble() / route2.distance()

        return route1IntersectionRatio >= thresholdRatio && route2IntersectionRatio >= thresholdRatio
    }
}