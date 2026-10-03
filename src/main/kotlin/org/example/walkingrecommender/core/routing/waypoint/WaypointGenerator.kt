package org.example.walkingrecommender.core.routing.waypoint

import org.example.walkingrecommender.core.model.Node

interface WaypointGenerator {

    fun getCandidates(): List<Node>

    fun generateWaypoints(): List<Node>

    fun generateWaypointPairs(): List<Pair<Node, Node>>
}