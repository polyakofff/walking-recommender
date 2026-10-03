package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.core.routing.CalculatedRoute

interface RouteScorer {

    fun score(route: CalculatedRoute): Double
}