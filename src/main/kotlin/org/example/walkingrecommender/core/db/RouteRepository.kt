package org.example.walkingrecommender.core.db

import org.example.walkingrecommender.core.model.DbRoute
import org.example.walkingrecommender.core.model.RouteType
import org.example.walkingrecommender.core.routing.ScoredRoute

interface RouteRepository {

    fun save(route: ScoredRoute, type: RouteType): DbRoute
}