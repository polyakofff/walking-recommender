package org.example.walkingrecommender.core.routing.waypoint

import org.example.walkingrecommender.core.model.Node

interface CandidateSelector {

    fun selectCandidates(): List<Node>
}