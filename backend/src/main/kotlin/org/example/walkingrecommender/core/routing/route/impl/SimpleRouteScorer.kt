package org.example.walkingrecommender.core.routing.route.impl

import org.example.walkingrecommender.properties.WalkingRecommenderProperties
import org.example.walkingrecommender.core.routing.CalculatedRoute
import org.example.walkingrecommender.core.routing.ScoredRoute
import org.example.walkingrecommender.core.routing.route.RouteScorer
import org.springframework.stereotype.Component
import kotlin.math.abs

@Component
class SimpleRouteScorer(
    private val properties: WalkingRecommenderProperties,
) : RouteScorer {

    override fun score(route: CalculatedRoute): ScoredRoute {
        val durationError = durationError(route)

        val repeatedDistanceError = repeatedDistanceError(route)

        val reverseDistanceError = reverseDistanceError(route)

        val score = route.attractiveness * properties.routeAttractivenessCoef -
                durationError * properties.routeDurationErrorCoef -
                repeatedDistanceError * properties.routeRepeatedDistanceErrorCoef -
                reverseDistanceError * properties.routeReverseDistanceErrorCoef +
                route.smoothness * properties.routeSmoothnessCoef

        return ScoredRoute(route, score)
    }

    fun durationError(route: CalculatedRoute): Double {
        return abs(route.duration.toDouble() - route.requestedDuration) / route.requestedDuration
    }

    fun repeatedDistanceError(route: CalculatedRoute): Double {
        return route.repeatedDistance.toDouble() / route.distance
    }

    fun reverseDistanceError(route: CalculatedRoute): Double {
        return route.reverseDistance.toDouble() / route.distance
    }
}