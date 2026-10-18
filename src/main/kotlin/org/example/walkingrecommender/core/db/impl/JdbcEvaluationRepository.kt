package org.example.walkingrecommender.core.db.impl

import org.example.walkingrecommender.core.db.EvaluationRepository
import org.example.walkingrecommender.core.evaluation.Evaluation
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JdbcEvaluationRepository(
    private val jdbcTemplate: JdbcTemplate,
) : EvaluationRepository {

    override fun save(evaluation: Evaluation) {
        jdbcTemplate.update(
            """
                INSERT INTO evaluation (
                    id,
                    user_id,
                    preferences_version,
                    route_id,
                    algorithm_version
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
            """.trimIndent(),
            evaluation.id,
            evaluation.userId,
            evaluation.preferencesVersion,
            evaluation.route.id,
            evaluation.algorithmVersion,
        )
    }

    override fun update(evaluationId: UUID, rating: Int, reasons: String) {
        jdbcTemplate.update(
            """
                UPDATE evaluation
                SET rating = ?,
                    reasons = ?,
                    answered_at = now()
                WHERE id = ?
            """.trimIndent(),
            rating,
            reasons,
            evaluationId,
        )
    }
}