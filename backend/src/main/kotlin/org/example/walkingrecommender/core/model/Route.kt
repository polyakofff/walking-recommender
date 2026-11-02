package org.example.walkingrecommender.core.model

import org.example.walkingrecommender.core.routing.ScoredRoute
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import java.util.UUID

data class DbRoute(
    val id: UUID,
    val type: RouteType,
    val geometry: LineString,
    val distance: Int,
    val duration: Int,
    val attractiveness: Double?,
    val score: Double,
)

enum class RouteType {
    PERSONALIZED_SCENIC,
    GENERALIZED_SCENIC,
    FASTEST,
}

fun ScoredRoute.toDbRoute(type: RouteType, geometryFactory: GeometryFactory): DbRoute {
    val coordinates = buildList {
        add(start().geometry.coordinate)

        edges().forEach { edge ->
            val coordinates = edge.geometry.coordinates.toList()
            val orientedCoordinates = when {
                coordinates.first() == last() -> coordinates
                coordinates.last() == last() -> coordinates.reversed()
                else -> error("Edge [${edge.id}] is not connected to previous edge in path")
            }
            addAll(orientedCoordinates.drop(1))
        }
    }

    return DbRoute(
        id = UUID.randomUUID(),
        type = type,
        geometry = geometryFactory.createLineString(coordinates.toTypedArray()),
        distance = distance(),
        duration = duration(),
        attractiveness = when (type) {
            RouteType.PERSONALIZED_SCENIC, RouteType.GENERALIZED_SCENIC -> attractiveness()
            else -> null
        },
        score = score,
    )
}
