package org.example.walkingrecommender.core.ab

import org.example.walkingrecommender.core.model.DbRoute
import java.util.UUID

data class Comparison(
    val id: UUID,
    val userId: UUID,
    val preferencesVersion: Int,
    val routeA: DbRoute,
    val routeB: DbRoute,
    val algorithmVersion: String,
    val choice: Choice? = null,
)

enum class Choice {
    A,
    B,
    EQUAL,
    NONE,
}
