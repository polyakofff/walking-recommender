package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.core.routing.CalculatedRoute
import org.example.walkingrecommender.core.routing.Route
import org.example.walkingrecommender.user.UserPreferences

interface RouteCalculator {

    fun calculate(route: Route,
                  userPreferences: UserPreferences,
                  requestedDuration: Int
    ): CalculatedRoute
}