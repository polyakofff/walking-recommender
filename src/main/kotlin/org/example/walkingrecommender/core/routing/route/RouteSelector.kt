package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.core.routing.CalculatedRoute

interface RouteSelector {

    fun select(routes: List<CalculatedRoute>): List<CalculatedRoute>
}