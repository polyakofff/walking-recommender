package org.example.walkingrecommender.core.db

import org.example.walkingrecommender.core.ab.Choice
import org.example.walkingrecommender.core.ab.Comparison
import java.util.UUID

interface ComparisonRepository {

    fun save(comparison: Comparison)

    fun update(comparisonId: UUID, choice: Choice)
}