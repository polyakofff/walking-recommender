package org.example.walkingrecommender.core.main

import org.example.walkingrecommender.core.model.SegmentId
import org.example.walkingrecommender.core.db.UrbanObjectRepository
import org.springframework.stereotype.Component

@Component
class ObjectsWithinEdgesFinder(
    private val urbanObjectRepository: UrbanObjectRepository,
) {

    fun find() {
        val edgeId = 19357L

        val map = urbanObjectRepository.findWithinObjects(listOf(SegmentId(edgeId, 0)), 50.0)

        println(map)
    }
}