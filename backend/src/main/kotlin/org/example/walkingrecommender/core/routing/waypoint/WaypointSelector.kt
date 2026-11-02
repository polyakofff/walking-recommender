package org.example.walkingrecommender.core.routing.waypoint

import org.example.walkingrecommender.core.model.Node

interface WaypointSelector {

    fun selectWaypoints(waypoints: List<Node>): List<Node>

    fun selectWaypointPairs(pairs: List<Pair<Node, Node>>): List<Pair<Node, Node>>
}