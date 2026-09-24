package org.example.walkingrecommender.urban.routing

import org.example.walkingrecommender.urban.Node

data class Route(
    val start: Node,
    val goal: Node,
    val paths: List<Path>,
)

data class CalculatedRoute(
    val route: Route,
    val distance: Int,
    val duration: Int,
    val attractiveness: Double,
    val repeatedDistance: Int,
    val reverseDistance: Int,
    val smoothness: Double,
    val requestedDuration: Int,
)