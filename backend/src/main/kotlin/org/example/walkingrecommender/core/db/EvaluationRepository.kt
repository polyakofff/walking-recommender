package org.example.walkingrecommender.core.db

import org.example.walkingrecommender.core.evaluation.Evaluation
import java.util.UUID

interface EvaluationRepository {

    fun save(evaluation: Evaluation)

    fun update(evaluationId: UUID, rating: Int, reasons: String)
}