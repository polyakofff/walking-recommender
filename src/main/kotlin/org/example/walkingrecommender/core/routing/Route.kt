package org.example.walkingrecommender.core.routing

import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.Node

data class Path(
    val start: Node,
    val end: Node,
    val edges: List<Edge>,
)

data class Route(
    val start: Node,
    val goal: Node,
    val paths: List<Path>,
) {
    fun edges(): List<Edge> = paths.flatMap { it.edges }
}

data class CalculatedRoute(
    val route: Route,
    val distance: Int,
    val duration: Int,
    val attractiveness: Double,
    val repeatedDistance: Int,
    val reverseDistance: Int,
    val smoothness: Double,
    val requestedDuration: Int,
) {
    fun start(): Node = route.start

    fun goal(): Node = route.goal

    fun paths(): List<Path> = route.paths

    fun edges(): List<Edge> = route.edges()
}

data class ScoredRoute(
    val calculatedRoute: CalculatedRoute,
    val score: Double,
) {
    fun start(): Node = calculatedRoute.start()

    fun goal(): Node = calculatedRoute.goal()

    fun paths(): List<Path> = calculatedRoute.paths()

    fun edges(): List<Edge> = calculatedRoute.edges()

    fun distance(): Int = calculatedRoute.distance

    fun duration(): Int = calculatedRoute.duration

    fun attractiveness(): Double = calculatedRoute.attractiveness

    fun repeatedDistance(): Int = calculatedRoute.repeatedDistance

    fun reverseDistance(): Int = calculatedRoute.reverseDistance

    fun smoothness(): Double = calculatedRoute.smoothness

    fun requestedDuration(): Int = calculatedRoute.requestedDuration
}