package org.example.walkingrecommender.api

import org.example.walkingrecommender.core.routing.CalculatedRoute

data class AbRequest(
    val start: Coordinate,
    val end: Coordinate,
    val durationMinutes: Int,
    val comparisonCount: Int,
)

data class VoteRequest(
    val comparisonId: String,
    val choice: Choice,
)

enum class Choice {
    A, B, EQUAL, NONE,
}

data class AbResponse(
    val comparisons: List<Comparison>,
)

data class Comparison(
    val id: String,
    val routeA: Route,
    val routeB: Route,
)

data class Route(
    val coordinates: List<Coordinate>,
    val distance: Int,
    val duration: Int,
    val attractiveness: Double,
)

data class Coordinate(
    val longitude: Double,
    val latitude: Double,
)

fun CalculatedRoute.toDto(): Route {
    val coordinates = buildList {
        add(route.start.geometry.coordinate)

        route.paths.forEach { path ->
            path.edges.forEach { edge ->
                val coordinates = edge.geometry.coordinates.toList()
                val orientedCoordinates = when {
                    coordinates.first() == last() -> coordinates
                    coordinates.last() == last() -> coordinates.reversed()
                    else -> error("Edge [${edge.id}] is not connected to previous edge in path")
                }
                addAll(orientedCoordinates.drop(1))
            }
        }
    }
        .map {
            Coordinate(
                it.x,
                it.y
            )
        }

    return Route(
        coordinates = coordinates,
        distance = distance,
        duration = duration,
        attractiveness = attractiveness,
    )
}