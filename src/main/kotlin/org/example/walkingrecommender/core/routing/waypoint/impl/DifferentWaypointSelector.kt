package org.example.walkingrecommender.core.routing.waypoint.impl

import org.example.walkingrecommender.properties.WalkingRecommenderProperties
import org.example.walkingrecommender.core.distance
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.routing.waypoint.WaypointSelector
import org.locationtech.jts.geom.Point

class DifferentWaypointSelector(
    private val waypointSelectDistanceMeters: Int,
) : WaypointSelector {

    override fun selectWaypoints(waypoints: List<Node>): List<Node> {
        val selected = mutableListOf<Node>()

        waypoints.forEachIndexed { index, candidate ->
            if (index % 10000 == 0) {
                println("candidates viewed: $index, selected: ${selected.size}")
            }
            val similar = selected.any { alreadySelected ->
                areSimilar(
                    alreadySelected.geometry,
                    candidate.geometry,
                )
            }
            if (!similar) {
                selected += candidate
            }
        }

        return selected
    }

    override fun selectWaypointPairs(pairs: List<Pair<Node, Node>>): List<Pair<Node, Node>> {
        val selected = mutableListOf<Pair<Node, Node>>()

        pairs.forEachIndexed { index, candidate ->
            if (index % 10000 == 0) {
                println("candidates viewed: $index, selected: ${selected.size}")
            }
            val similar = selected.any { alreadySelected ->
                areSimilar(
                    alreadySelected.first.geometry,
                    candidate.first.geometry,
                ) && areSimilar(
                    alreadySelected.second.geometry,
                    candidate.second.geometry,
                )
            }
            if (!similar) {
                selected += candidate
            }
        }

        return selected
    }

    private fun areSimilar(point1: Point, point2: Point): Boolean {
        return distance(point1, point2) <= waypointSelectDistanceMeters
    }
}