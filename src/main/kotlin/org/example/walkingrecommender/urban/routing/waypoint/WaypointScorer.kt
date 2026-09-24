package org.example.walkingrecommender.urban.routing.waypoint

import org.example.walkingrecommender.urban.Node

interface WaypointScorer {

    fun scoreWaypoint(node: Node): Double

    fun scoreWaypointPair(pair: Pair<Node, Node>): Double
}