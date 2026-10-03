package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.core.routing.CalculatedRoute
import org.springframework.stereotype.Component
import kotlin.math.abs

@Component
class SimpleRouteScorer : RouteScorer {

    private val attractivenessCoef = 1.0
    private val durationErrorCoef = 1.2
    private val repeatedDistanceErrorCoef = 0.5
    private val reverseDistanceErrorCoef = 3.0
    private val smoothnessCoef = 0.5

    override fun score(route: CalculatedRoute): Double {
        val durationError = durationError(route)

        val repeatedDistanceError = repeatedDistanceError(route)

        val reverseDistanceError = reverseDistanceError(route)

        return route.attractiveness * attractivenessCoef -
                durationError * durationErrorCoef -
                repeatedDistanceError * repeatedDistanceErrorCoef -
                reverseDistanceError * reverseDistanceErrorCoef +
                route.smoothness * smoothnessCoef
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