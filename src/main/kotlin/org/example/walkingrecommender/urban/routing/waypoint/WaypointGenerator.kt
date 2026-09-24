package org.example.walkingrecommender.urban.routing.waypoint

import org.example.walkingrecommender.urban.Node

interface WaypointGenerator {

    fun getCandidates(): List<Node>

    fun generateWaypoints(): List<Node>

    fun generateWaypointPairs(): List<Pair<Node, Node>>
}