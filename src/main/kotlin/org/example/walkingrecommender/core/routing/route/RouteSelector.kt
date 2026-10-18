package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.core.routing.ScoredRoute

interface RouteSelector {

    fun select(routes: List<ScoredRoute>): List<ScoredRoute>
}