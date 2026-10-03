package org.example.walkingrecommender.core.routing.route

import org.example.walkingrecommender.core.northAngle
import org.example.walkingrecommender.core.routing.CalculatedRoute
import org.example.walkingrecommender.core.routing.Route
import org.example.walkingrecommender.core.routing.attractiveness
import org.example.walkingrecommender.user.UserPreferences
import org.springframework.stereotype.Component
import kotlin.collections.sumOf
import kotlin.math.abs
import kotlin.math.min

@Component
class SimpleRouteCalculator : RouteCalculator {

    override fun calculate(
        route: Route,
        userPreferences: UserPreferences,
        requestedDuration: Int,
    ): CalculatedRoute {
        val edges = route.paths.flatMap { it.edges }

        val totalDistance = edges.sumOf { it.distance }
        val totalDuration = edges.sumOf { it.walkingTime }

        val attractiveness = attractiveness(route, userPreferences, totalDistance)

        val repeatedDistance = repeatedDistance(route)

        val reverseDistance = reverseDistance(route)

        val smoothness = smoothness(route)

        // TODO: учитывать параллельные улицы

        return CalculatedRoute(
            route = route,
            distance = totalDistance,
            duration = totalDuration,
            attractiveness = attractiveness,
            repeatedDistance = repeatedDistance,
            reverseDistance = reverseDistance,
            smoothness = smoothness,
            requestedDuration = requestedDuration,
        )
    }

    private fun attractiveness(
        route: Route,
        userPreferences: UserPreferences,
        totalDistance: Int
    ): Double {
        return route.paths
            .flatMap { it.edges }
            .sumOf { edge ->
                attractiveness(edge, userPreferences) * edge.distance
            } / totalDistance
    }

    private fun repeatedDistance(route: Route): Int {
        return route.paths
            .flatMap { it.edges }
            .groupBy { it.id }
            .filterValues { list -> list.size > 1 }
            .values
            .sumOf { list -> list.first().distance * (list.size - 1) }
    }

    private fun reverseDistance(route: Route): Int {
        return (route.paths + route.paths.first())
            .zipWithNext { path, nextPath ->
                path.edges.reversed()
                    .zip(nextPath.edges)
                    .takeWhile { (edge1, edge2) -> edge1.id == edge2.id }
                    .sumOf { (edge, _) -> edge.distance }
            }
            .sum()
    }

    private fun smoothness(route: Route): Double {
        val turnAngles = route.paths
            .flatMap { it.edges }
            .zipWithNext { edge, nextEdge ->
                val (p0, p1, p2) = when {
                    edge.nodeAOsmId == nextEdge.nodeAOsmId ->
                        Triple(
                            edge.geometry.coordinates[0],
                            edge.geometry.coordinates[1],
                            nextEdge.geometry.coordinates[1]
                        )
                    edge.nodeAOsmId == nextEdge.nodeBOsmId ->
                        Triple(
                            edge.geometry.coordinates[0],
                            edge.geometry.coordinates[1],
                            nextEdge.geometry.coordinates[nextEdge.geometry.coordinates.lastIndex - 1]
                        )
                    edge.nodeBOsmId == nextEdge.nodeAOsmId ->
                        Triple(
                            edge.geometry.coordinates[edge.geometry.coordinates.lastIndex],
                            edge.geometry.coordinates[edge.geometry.coordinates.lastIndex - 1],
                            nextEdge.geometry.coordinates[1]
                        )
                    edge.nodeBOsmId == nextEdge.nodeBOsmId ->
                        Triple(
                            edge.geometry.coordinates[edge.geometry.coordinates.lastIndex],
                            edge.geometry.coordinates[edge.geometry.coordinates.lastIndex - 1],
                            nextEdge.geometry.coordinates[nextEdge.geometry.coordinates.lastIndex - 1]
                        )
                    else -> error("Edges are not connected")
                }

                val a1 = northAngle(p1, p0)
                val a2 = northAngle(p0, p2)

                val diff = abs(a2 - a1)
                min(diff, 360.0 - diff)
            }

        val mse = turnAngles
            .map { angle ->
//                val x = angle / 180.0
                val x = (angle / 90.0).coerceAtMost(1.0)
                x * x
            }
            .average()

        return 1.0 - mse
    }
}