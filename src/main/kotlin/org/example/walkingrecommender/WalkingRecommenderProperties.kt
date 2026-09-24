package org.example.walkingrecommender

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(
    prefix = "walking-recommender"
)
data class WalkingRecommenderProperties(
    val edgeSegmentMaxMeters: Double,
)
