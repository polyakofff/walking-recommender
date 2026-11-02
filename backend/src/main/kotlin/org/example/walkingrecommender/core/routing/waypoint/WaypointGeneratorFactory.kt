package org.example.walkingrecommender.core.routing.waypoint

import org.example.walkingrecommender.properties.WalkingRecommenderProperties
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.routing.waypoint.impl.AdjacentDistanceCandidateSelector
import org.example.walkingrecommender.core.routing.waypoint.impl.ByTimeAndShapeWaypointScorer
import org.example.walkingrecommender.core.routing.waypoint.impl.DifferentWaypointSelector
import org.example.walkingrecommender.core.routing.waypoint.impl.SimpleWaypointGenerator
import org.springframework.stereotype.Component

@Component
class WaypointGeneratorFactory(
    private val properties: WalkingRecommenderProperties,
) {

    fun create(
        graph: Graph,
        start: Node,
        goal: Node,
        startToNodeTime: Map<Long, Int>,
        goalToNodeTime: Map<Long, Int>,
        desiredTime: Int,
    ): WaypointGenerator {
        return SimpleWaypointGenerator(
            start = start,
            goal = goal,
            startToNodeTime = startToNodeTime,
            goalToNodeTime = goalToNodeTime,
            desiredTime = desiredTime.toDouble(),
            candidateSelector = AdjacentDistanceCandidateSelector(
                graph = graph,
                startToNodeTime = startToNodeTime,
                candidateDistanceMeters = properties.candidateDistanceMeters,
            ),
            waypointScorer = ByTimeAndShapeWaypointScorer(
                start = start,
                goal = goal,
                startToNodeTime = startToNodeTime,
                goalToNodeTime = goalToNodeTime,
                desiredTime = desiredTime.toDouble(),
                timeWeight = properties.waypointScorerTimeWeight,
            ),
            waypointSelector = DifferentWaypointSelector(
                waypointSelectDistanceMeters = properties.waypointSelectorDistanceMeters,
            ),
            properties = properties,
        )
    }
}