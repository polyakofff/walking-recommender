package org.example.walkingrecommender.api.dto

import org.example.walkingrecommender.core.model.DbRoute

data class Route(
    val coordinates: List<Coordinate>,
    val distance: Int,
    val duration: Int,
    val attractiveness: Double,
    val score: Double,
)

data class Coordinate(
    val longitude: Double,
    val latitude: Double,
)

fun DbRoute.toDto(): Route {
    val coordinates = geometry.coordinates.map {
        Coordinate(it.x, it.y)
    }

    return Route(
        coordinates = coordinates,
        distance = distance,
        duration = duration,
        attractiveness = attractiveness ?: 0.0,
        score = score,
    )
}