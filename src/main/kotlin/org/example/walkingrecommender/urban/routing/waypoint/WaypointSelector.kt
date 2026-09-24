package org.example.walkingrecommender.urban.routing.waypoint

import org.example.walkingrecommender.urban.Node

interface WaypointSelector {

    fun selectWaypoints(waypoints: List<Node>): List<Node>

    fun selectWaypointPairs(pairs: List<Pair<Node, Node>>): List<Pair<Node, Node>>
}