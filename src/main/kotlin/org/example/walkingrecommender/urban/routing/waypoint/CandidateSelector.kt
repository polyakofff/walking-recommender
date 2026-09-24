package org.example.walkingrecommender.urban.routing.waypoint

import org.example.walkingrecommender.urban.Node

interface CandidateSelector {

    fun selectCandidates(): List<Node>
}