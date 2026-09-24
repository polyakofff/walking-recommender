package org.example.walkingrecommender.urban.routing.waypoint

import org.example.walkingrecommender.urban.Node
import org.example.walkingrecommender.urban.distance
import org.example.walkingrecommender.urban.externalId
import org.example.walkingrecommender.urban.time
import org.locationtech.jts.geom.Point
import kotlin.math.abs
import kotlin.math.sqrt

class ByTimeAndShapeWaypointScorer(
    private val start: Node,
    private val goal: Node,
    private val startToNodeTime: Map<Long, Int>,
    private val goalToNodeTime: Map<Long, Int>,
    private val desiredTime: Double,
) : WaypointScorer {

    private val timeWeight = 0.5

    override fun scoreWaypoint(node: Node): Double {
        return timeWeight * timeScore(node) +
                (1.0 - timeWeight) * shapeScore(node.geometry, start.geometry, goal.geometry)
    }

    override fun scoreWaypointPair(pair: Pair<Node, Node>): Double {
        return timeWeight * timeScore(pair) +
                (1.0 - timeWeight) * shapeScore(pair.first.geometry, pair.second.geometry, start.geometry)
    }

    // score in 0..1
    private fun timeScore(node: Node): Double {
        val timeToStart = startToNodeTime[node.externalId()]!!
        val timeToGoal = goalToNodeTime[node.externalId()]!!
        val totalTime = timeToStart + timeToGoal
        return 1.0 - abs(totalTime - desiredTime) / desiredTime
    }

    // score in 0..1
    private fun timeScore(pair: Pair<Node, Node>): Double {
        val timeToStart = startToNodeTime[pair.first.externalId()]!!
        val timeToGoal = goalToNodeTime[pair.second.externalId()]!!

        val directDistance = distance(pair.first, pair.second)
        val directTime = time(directDistance)
        val expectedTime = directTime * 1.5

        val totalTime = timeToStart + timeToGoal + expectedTime
        return 1.0 - abs(totalTime - desiredTime) / desiredTime
    }

    // score in 0..1
    private fun shapeScore(point1: Point, point2: Point, point3: Point): Double {
        val a = distance(point1, point2)
        val b = distance(point2, point3)
        val c = distance(point3, point1)

        val perimeter = a + b + c
        val s = perimeter / 2.0

        val areaSquared = s * (s - a) * (s - b) * (s - c)
        val area = sqrt(areaSquared)

        return 12.0 * sqrt(3.0) * area / (perimeter * perimeter)
    }
}