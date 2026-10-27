package org.example.walkingrecommender.core.evaluation

import org.example.walkingrecommender.core.model.DbRoute
import java.util.UUID

data class Evaluation(
    val id: UUID,
    val userId: UUID,
    val preferencesVersion: Int,
    val route: DbRoute,
    val algorithmVersion: String,
    val rating: Int? = null,
    val reasons: String? = null,
)

const val MAX_RATING = 5

enum class BadRatingReason {
    TOO_LONG,
    TOO_SHORT,
    ROADWAY_PASSES,
    INVALID_WAY,
    // TODO
}

data class ReasonItem(
    val reason: BadRatingReason,
    val text: String,
)

object ReasonItems {
    val reasons = listOf(
        ReasonItem(
            BadRatingReason.TOO_LONG,
            "Маршрут слишком длинный"
        ),
        ReasonItem(
            BadRatingReason.TOO_SHORT,
            "Маршрут слишком короткий"
        ),
        ReasonItem(
            BadRatingReason.ROADWAY_PASSES,
            "Маршрут проходит по проезжей части"
        ),
        ReasonItem(
            BadRatingReason.INVALID_WAY,
            "Маршрут содержит невалидный участок"
        )
    )
}
