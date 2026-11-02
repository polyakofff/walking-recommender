package org.example.walkingrecommender.core.routing

import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.SegmentFeatures
import org.example.walkingrecommender.user.Preferences
import kotlin.math.abs

/**
 * Edge attractiveness in [-1..1]
 */
fun attractiveness(edge: Edge, prefs: Preferences): Double {
    requireNotNull(edge.segments)

    val totalDistance = edge.segments.sumOf { it.distanceMeters }

    return edge.segments.sumOf { segment ->
        requireNotNull(segment.features)

        attractiveness(segment.features, prefs) * segment.distanceMeters
    } / totalDistance
}

/**
 * Segment attractiveness in [-1..1]
 */
fun attractiveness(f: SegmentFeatures, p: Preferences): Double {
    var like = 0.0
    var dislike = 0.0
    var totalWeight = 0.0

    fun add(feature: Double, preference: Double) {
        when {
            preference > 0.0 -> {
                // like presence, dislike absence
                like += feature * preference
                dislike += (1.0 - feature) * preference
            }
            preference < 0.0 -> {
                // like absence, dislike presence
                like += (1.0 - feature) * -preference
                dislike += feature * -preference
            }
        }
        totalWeight += abs(preference)
    }

    add(f.greenery, p.greenery)
    add(f.water, p.water)
    add(f.traffic, p.traffic)
    add(f.noise, p.noise)
    add(f.historicArchitecture, p.historicArchitecture)
    add(f.modernArchitecture, p.modernArchitecture)
    add(f.commercialActivity, p.commercialActivity)
    add(f.grittiness, p.grittiness)
    add(f.pedestrianComfort, p.pedestrianComfort)

    if (totalWeight == 0.0) {
        return 0.0
    }

    return (like - dislike) / totalWeight
}