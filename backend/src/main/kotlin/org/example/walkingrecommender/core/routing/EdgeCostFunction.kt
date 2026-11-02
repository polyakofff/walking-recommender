package org.example.walkingrecommender.core.routing

import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.user.Preferences

sealed interface EdgeCostFunction {
    fun cost(edge: Edge): Double

    val minCostMultiplier: Double
}

class FastestCostFunction : EdgeCostFunction {
    override fun cost(edge: Edge): Double = edge.walkingTime.toDouble()

    override val minCostMultiplier: Double = 1.0
}

class ScenicCostFunction(
    private val preferences: Preferences,
    val scenicWeight: Double,
) : EdgeCostFunction {
    override fun cost(edge: Edge): Double {
        val attractiveness = attractiveness(edge, preferences)
        return edge.walkingTime * (1.0 - scenicWeight * attractiveness)
    }

    override val minCostMultiplier: Double = 1.0 - scenicWeight
}