package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.core.routing.CalculatedRoute
import org.example.walkingrecommender.core.routing.Route
import org.example.walkingrecommender.user.Preferences

interface RouteCalculator {

    fun calculate(route: Route,
                  preferences: Preferences,
                  requestedDuration: Int
    ): CalculatedRoute
}