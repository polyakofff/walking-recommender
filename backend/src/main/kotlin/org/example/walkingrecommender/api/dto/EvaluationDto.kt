package org.example.walkingrecommender.api.dto

import org.example.walkingrecommender.core.evaluation.BadRatingReason
import org.example.walkingrecommender.core.evaluation.ReasonItem
import java.util.UUID

data class EvaluationRequest(
    val username: String,
    val start: Coordinate,
    val end: Coordinate,
    val durationMinutes: Int,
    val count: Int,
)

data class EvaluateRequest(
    val evaluationId: UUID,
    val rating: Int,
    val reasons: List<BadRatingReason>,
    val other: String?,
)

data class EvaluationResponse(
    val maxRating: Int,
    val reasons: List<ReasonDto>,
    val evaluations: List<Evaluation>,
)

data class ReasonDto(
    val value: BadRatingReason,
    val text: String,
)

fun ReasonItem.toDto(): ReasonDto {
    return ReasonDto(
        value = reason,
        text = text,
    )
}

data class Evaluation(
    val id: UUID,
    val route: Route,
)
