package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.core.routing.CalculatedRoute
import org.example.walkingrecommender.core.routing.ScoredRoute

interface RouteScorer {

    fun score(route: CalculatedRoute): ScoredRoute
}