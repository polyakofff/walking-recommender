package org.example.walkingrecommender.urban.routing.route

import org.example.walkingrecommender.urban.routing.CalculatedRoute

interface RouteScorer {

    fun score(route: CalculatedRoute): Double
}