package org.example.walkingrecommender.core.routing.waypoint

import org.example.walkingrecommender.core.model.Node

interface WaypointScorer {

    fun scoreWaypoint(node: Node): Double

    fun scoreWaypointPair(pair: Pair<Node, Node>): Double
}