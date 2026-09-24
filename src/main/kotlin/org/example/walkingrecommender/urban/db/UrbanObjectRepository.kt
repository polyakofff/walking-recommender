package org.example.walkingrecommender.urban.db

import org.example.walkingrecommender.urban.SegmentId
import org.example.walkingrecommender.urban.UrbanObject

interface UrbanObjectRepository {

    fun save(objects: List<UrbanObject>)

    fun findWithinObjects(segmentIds: List<SegmentId>, radiusMeters: Double): Map<SegmentId, List<NearbyUrbanObject>>
}