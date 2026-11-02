package org.example.walkingrecommender.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(
    prefix = "walking-recommender"
)
data class WalkingRecommenderProperties(
    val edgeSegmentMaxMeters: Double,

    val withinObjectsRadiusMeters: Int,

    val scenicWeight: Double,

    // route selector
    val routeDifferenceThresholdRatio: Double,

    // route scorer
    val routeAttractivenessCoef: Double,
    val routeDurationErrorCoef: Double,
    val routeRepeatedDistanceErrorCoef: Double,
    val routeReverseDistanceErrorCoef: Double,
    val routeSmoothnessCoef: Double,

    // generator
    val generatorDirectDistanceFactor: Double,

    // candidate selector
    val candidateDistanceMeters: Int,

    // waypoint scorer
    val waypointScorerTimeWeight: Double,
    // waypoint selector
    val waypointSelectorDistanceMeters: Int,
    // waypoint generator
    val waypointGeneratorLimit: Int,
    val waypointGeneratorPairsLimit: Int,
)