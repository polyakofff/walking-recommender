package org.example.walkingrecommender.core.main

import org.example.walkingrecommender.core.routing.PathFinder
import org.example.walkingrecommender.core.routing.ScenicCostFunction
import org.example.walkingrecommender.user.UserPreferences
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Component
import java.io.File

@Component
class PathFinderTest(
    private val geometryFactory: GeometryFactory,
    private val pathFinder: PathFinder,
) {

    fun run() {

        val from = geometryFactory.createPoint(Coordinate(37.236000, 55.677697))
        val to = geometryFactory.createPoint(Coordinate(37.267007, 55.681831))

        val prefs = UserPreferences(
            greenery = 0.3,
            water = 1.0,
            traffic = -1.0,
            noise = 0.0,
            historicArchitecture = 0.0,
            modernArchitecture = 0.0,
            commercialActivity = 0.0,
            grittiness = 0.0,
            pedestrianComfort = 0.0
        )

        val costF = ScenicCostFunction(prefs, scenicWeight = 0.99)

        val path = pathFinder.find(
            from,
            to,
            costF,
        )

        val exporter = GeoJsonExporter()

//        exporter.exportGraph(
//            pathFinder.graph()!!,
//            File("data/geojson/graph.geojson"),
//            edgeCostFunction = { edge -> costF.cost(edge) },
//        )

        path?.let {
            exporter.exportLines(
                it.lines,
                File("data/geojson/path.geojson"),
            )
        }

    }
}