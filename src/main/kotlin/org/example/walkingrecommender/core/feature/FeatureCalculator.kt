package org.example.walkingrecommender.core.feature

import org.example.walkingrecommender.core.db.impl.NearbyUrbanObject
import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.EdgeSegment

interface FeatureCalculator {

    fun version(): Int

    fun calculateSegment(
        edge: Edge,
        segment: EdgeSegment,
        withinObjects: List<NearbyUrbanObject>,
    ): Features
}