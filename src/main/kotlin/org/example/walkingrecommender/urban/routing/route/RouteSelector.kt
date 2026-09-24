package org.example.walkingrecommender.urban.routing.route

import org.example.walkingrecommender.urban.routing.CalculatedRoute

interface RouteSelector {

    fun select(routes: List<CalculatedRoute>): List<CalculatedRoute>
}