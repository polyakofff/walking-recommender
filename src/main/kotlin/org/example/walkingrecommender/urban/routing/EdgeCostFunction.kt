package org.example.walkingrecommender.urban.routing

import org.example.walkingrecommender.urban.Edge
import org.example.walkingrecommender.user.UserPreferences

sealed interface EdgeCostFunction {
    fun cost(edge: Edge): Double

    val minCostMultiplier: Double
}

class FastestCostFunction : EdgeCostFunction {
    override fun cost(edge: Edge): Double =
        edge.walkingTime.toDouble()

    override val minCostMultiplier: Double = 1.0
}

class ScenicCostFunction(
    private val userPreferences: UserPreferences,
    val scenicWeight: Double = 0.5,
) : EdgeCostFunction {
    override fun cost(edge: Edge): Double {
        val attractiveness = attractiveness(edge, userPreferences)
        return edge.walkingTime * (1.0 - scenicWeight * attractiveness)
    }

    override val minCostMultiplier: Double = 1.0 - scenicWeight
}