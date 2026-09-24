package org.example.walkingrecommender.urban.routing.route

import org.example.walkingrecommender.urban.routing.CalculatedRoute
import org.example.walkingrecommender.urban.routing.Route
import org.example.walkingrecommender.user.UserPreferences

interface RouteCalculator {

    fun calculate(route: Route,
                  userPreferences: UserPreferences,
                  requestedDuration: Int
    ): CalculatedRoute
}