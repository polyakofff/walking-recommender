package org.example.walkingrecommender.core.db

import org.example.walkingrecommender.core.db.impl.NearbyUrbanObject
import org.example.walkingrecommender.core.model.SegmentId
import org.example.walkingrecommender.core.model.UrbanObject

interface UrbanObjectRepository {

    fun save(objects: List<UrbanObject>)

    fun findWithinObjects(segmentIds: List<SegmentId>, radiusMeters: Double): Map<SegmentId, List<NearbyUrbanObject>>
}