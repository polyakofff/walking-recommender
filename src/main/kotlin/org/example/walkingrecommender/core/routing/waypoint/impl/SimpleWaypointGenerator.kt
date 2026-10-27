package org.example.walkingrecommender.core.routing.waypoint.impl

import org.example.walkingrecommender.properties.WalkingRecommenderProperties
import org.example.walkingrecommender.core.distance
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.model.externalId
import org.example.walkingrecommender.core.northAngle
import org.example.walkingrecommender.core.routing.waypoint.CandidateSelector
import org.example.walkingrecommender.core.routing.waypoint.WaypointGenerator
import org.example.walkingrecommender.core.routing.waypoint.WaypointScorer
import org.example.walkingrecommender.core.routing.waypoint.WaypointSelector
import org.example.walkingrecommender.core.time
import org.locationtech.jts.geom.Point
import org.slf4j.LoggerFactory

class SimpleWaypointGenerator(
    private val start: Node,
    private val goal: Node,
    private val startToNodeTime: Map<Long, Int>,
    private val goalToNodeTime: Map<Long, Int>,
    private val desiredTime: Double,
    private val candidateSelector: CandidateSelector,
    private val waypointScorer: WaypointScorer,
    private val waypointSelector: WaypointSelector,
    private val properties: WalkingRecommenderProperties,
) : WaypointGenerator {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val candidates = candidateSelector.selectCandidates()

    init {
        logger.info("candidates: ${candidates.size}")
    }

    override fun getCandidates(): List<Node> = candidates

    override fun generateWaypoints(): List<Node> {
        val waypoints = candidates
            .byTimeToStartRatio(0.25) // TODO

            .byTimeToGoalRatio(0.25) // TODO

            // общее время не слишком большое
            .filter { node ->
                val timeToStart = startToNodeTime[node.externalId()]!!
                val timeToGoal = goalToNodeTime[node.externalId()]!!

                val totalTime = timeToStart + timeToGoal

                val timeError = (totalTime - desiredTime) / desiredTime
                timeError <= 1.0
            }

            .sortedByDescending { node -> waypointScorer.scoreWaypoint(node) }

        logger.info("waypoints: ${waypoints.size}")

        // TODO: временная заглушка, покуда DiverseWaypointSelector.threshold <= SimpleCandidateSelector.distance
//        val finalWaypoints = waypointSelector.selectWaypoints(waypoints)
        val finalWaypoints = waypoints

        logger.info("final waypoints: ${finalWaypoints.size}")

        return finalWaypoints.take(properties.waypointGeneratorLimit)
    }

    override fun generateWaypointPairs(): List<Pair<Node, Node>> {
        val firstWaypoints = candidates
            .byTimeToStartRatio(lowerBound = 0.25, upperBound = 0.5) // TODO

        val secondWaypoints = candidates
            .byTimeToGoalRatio(lowerBound = 0.25, upperBound = 0.5) // TODO

        val pairs = firstWaypoints.flatMap { n1 ->
            secondWaypoints
                .filter { n2 -> n1.externalId() != n2.externalId() }

                // n1-start-n2 образуют угол
                .filter { n2 ->
                    angleDifference(start.geometry, n1.geometry, n2.geometry) in 30.0..150.0 // TODO
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

        logger.info("pairs: ${pairs.size}")

//        val finalPairs = waypointSelector.selectWaypointPairs(pairs)
        val finalPairs = pairs

        logger.info("final pairs: ${finalPairs.size}")

        return finalPairs.take(properties.waypointGeneratorPairsLimit)
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

    private fun angleDifference(vertex: Point, point1: org.locationtech.jts.geom.Point, point2: org.locationtech.jts.geom.Point): Double {
        val a1 = northAngle(vertex.coordinate, point1.coordinate)
        val a2 = northAngle(vertex.coordinate, point2.coordinate)

        return (a2 - a1 + 360.0) % 360.0
    }
}