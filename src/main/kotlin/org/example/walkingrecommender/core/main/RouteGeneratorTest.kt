package org.example.walkingrecommender.core.main

import org.example.walkingrecommender.core.routing.ScenicCostFunction
import org.example.walkingrecommender.core.routing.route.RouteGenerator
import org.example.walkingrecommender.user.UserPreferences
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Component
import java.io.File
import kotlin.math.abs

@Component
class RouteGeneratorTest(
    private val geometryFactory: GeometryFactory,
    private val routeGenerator: RouteGenerator,
) {

    fun run() {
//        val from = geometryFactory.createPoint(Coordinate(37.236000, 55.677697)) // Клубничное поле 3
//        val to = from

//        val from = geometryFactory.createPoint(Coordinate(37.270134, 55.674826)) // Маршала Неделина, 9
//        val to = from

        val from = geometryFactory.createPoint(Coordinate(37.412797, 55.734359)) // Партизанская, 22
        val to = from

        val prefs = UserPreferences(
            greenery = 0.3,
            water = 0.5,
            traffic = -0.5,
            noise = -0.3,
            historicArchitecture = 0.0,
            modernArchitecture = 0.5,
            commercialActivity = 0.0,
            grittiness = -1.0,
            pedestrianComfort = 1.0
        )

        val scenicWeight = 0.9
        val costF = ScenicCostFunction(prefs, scenicWeight = scenicWeight)

        val (routes, data) = routeGenerator.generate(
            startPoint = from,
            goalPoint = to,
            durationSeconds = 60 * 60,
            userPreferences = prefs,
            scenicWeight = scenicWeight,
        )

        val exporter = GeoJsonExporter()

        exporter.exportGraph(
            data.graph,
            from,
            data.candidates.map { it.geometry },
            File("data/geojson/graph.geojson"),
            edgeCostFunction = { edge -> costF.cost(edge) },
        )

        val someRoute = routes.maxBy { it.attractiveness }
//        val someRoute = routes[0]

        println("route: distance=${someRoute.distance}, duration=${someRoute.duration} attractiveness=${someRoute.attractiveness}, repeated distance=${someRoute.repeatedDistance}")

        routes.drop(0).take(10).forEachIndexed { index, route ->
            println("$index: attractiveness=${route.attractiveness}, duration=${route.duration}, distance=${route.distance}, repeatedDistance=${route.repeatedDistance}, reverseDistance=${route.reverseDistance}, smoothness=${route.smoothness}, durationError=${abs(route.duration.toDouble() - route.requestedDuration) / route.requestedDuration}, repeatedDistanceError=${route.repeatedDistance.toDouble() / route.distance}, reverseDistanceError=${route.reverseDistance.toDouble() / route.distance}")

            exporter.exportRoute(
                route,
                File("data/geojson/route/${index}.geojson"),
            )
        }
        exporter.exportRoute(
            someRoute,
            File("data/geojson/route/r.geojson"),
        )

//        route.let {
//            exporter.exportRoute(
//                it.route,
//                File("data/geojson/route.geojson"),
//            )
//        }
    }
}