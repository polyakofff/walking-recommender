package org.example.walkingrecommender.api.dto

import org.example.walkingrecommender.core.ab.Choice
import java.util.UUID

data class AbRequest(
    val username: String,
    val start: Coordinate,
    val end: Coordinate,
    val durationMinutes: Int,
    val count: Int,
)

data class VoteRequest(
    val comparisonId: UUID,
    val choice: Choice,
)

data class AbResponse(
    val comparisons: List<Comparison>,
)

data class Comparison(
    val id: UUID,
    val routeA: Route,
    val routeB: Route,
)
