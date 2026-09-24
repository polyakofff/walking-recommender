package org.example.walkingrecommender.urban.routing.waypoint

import org.example.walkingrecommender.urban.Node
import org.example.walkingrecommender.urban.distance
import org.example.walkingrecommender.urban.externalId
import org.example.walkingrecommender.urban.northAngle
import org.example.walkingrecommender.urban.time
import org.locationtech.jts.geom.Point

class SimpleWaypointGenerator(
    private val start: Node,
    private val goal: Node,
    private val startToNodeTime: Map<Long, Int>,
    private val goalToNodeTime: Map<Long, Int>,
    private val desiredTime: Double,
    private val candidateSelector: CandidateSelector,
    private val waypointScorer: WaypointScorer,
    private val waypointSelector: WaypointSelector,
) : WaypointGenerator {

    private val limit = 1000
    private val pairsLimit = 1000

    private val candidates = candidateSelector.selectCandidates()

    init {
        println("candidates: ${candidates.size}")
    }

    override fun getCandidates(): List<Node> = candidates

    override fun generateWaypoints(): List<Node> {
        val waypoints = candidates
            .byTimeToStartRatio(0.25)

            .byTimeToGoalRatio(0.25)

            // общее время не слишком большое
            .filter { node ->
                val timeToStart = startToNodeTime[node.externalId()]!!
                val timeToGoal = goalToNodeTime[node.externalId()]!!

                val totalTime = timeToStart + timeToGoal

                val timeError = (totalTime - desiredTime) / desiredTime
                timeError <= 1.0
            }

            .sortedByDescending { node -> waypointScorer.scoreWaypoint(node) }

        println("waypoints: ${waypoints.size}")

        val finalWaypoints = waypointSelector.selectWaypoints(waypoints)

        println("final waypoints: ${finalWaypoints.size}")

        return finalWaypoints.take(limit)
    }

    override fun generateWaypointPairs(): List<Pair<Node, Node>> {
        val firstWaypoints = candidates
            .byTimeToStartRatio(lowerBound = 0.25, upperBound = 0.5)

        val secondWaypoints = candidates
            .byTimeToGoalRatio(lowerBound = 0.25, upperBound = 0.5)

        val pairs = firstWaypoints.flatMap { n1 ->
            secondWaypoints
                .filter { n2 -> n1.externalId() != n2.externalId() }

                // n1-start-n2 образуют угол
                .filter { n2 ->
                    angleDifference(start.geometry, n1.geometry, n2.geometry) in 30.0..150.0
                }

                // минимальное общее время не слишком большое
                .filter { n2 ->
                    val directDistance = distance(n1, n2)
                    val directTime = time(directDistance)

                    val minTotalTime = startToNodeTime[n1.externalId()]!! + goalToNodeTime[n2.externalId()]!! + directTime

                    val timeError = (minTotalTime - desiredTime) / desiredTime
                    timeError <= 1.0
                }

                .map { n2 -> n1 to n2 }
        }
            .sortedByDescending { pair -> waypointScorer.scoreWaypointPair(pair) }

        println("pairs: ${pairs.size}")

        val finalPairs = waypointSelector.selectWaypointPairs(pairs)

        println("final pairs: ${finalPairs.size}")

        return finalPairs.take(pairsLimit)
    }

    private fun List<Node>.byTimeToStartRatio(lowerBound: Double, upperBound: Double? = null): List<Node> {
        return this.filter { node ->
            val timeToStart = startToNodeTime[node.externalId()]!!
            val ratio = timeToStart / desiredTime
            ratio >= lowerBound && (upperBound == null || ratio <= upperBound)
        }
    }

    private fun List<Node>.byTimeToGoalRatio(lowerBound: Double, upperBound: Double? = null): List<Node> {
        return this.filter { node ->
            val timeToGoal = goalToNodeTime[node.externalId()]!!
            val ratio = timeToGoal / desiredTime
            ratio >= lowerBound && (upperBound == null || ratio <= upperBound)
        }
    }

    private fun angleDifference(vertex: Point, point1: Point, point2: Point): Double {
        val a1 = northAngle(vertex.coordinate, point1.coordinate)
        val a2 = northAngle(vertex.coordinate, point2.coordinate)

        return (a2 - a1 + 360.0) % 360.0
    }
}