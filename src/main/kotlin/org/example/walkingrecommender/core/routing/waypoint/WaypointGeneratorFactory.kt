package org.example.walkingrecommender.core.routing.waypoint

import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.Node
import org.springframework.stereotype.Component

@Component
class WaypointGeneratorFactory {

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
            candidateSelector = SimpleCandidateSelector(
                graph = graph,
                startToNodeTime = startToNodeTime,
            ),
            waypointScorer = ByTimeAndShapeWaypointScorer(
                start = start,
                goal = goal,
                startToNodeTime = startToNodeTime,
                goalToNodeTime = goalToNodeTime,
                desiredTime = desiredTime.toDouble(),
            ),
            waypointSelector = DiverseWaypointSelector(),
        )
    }
}