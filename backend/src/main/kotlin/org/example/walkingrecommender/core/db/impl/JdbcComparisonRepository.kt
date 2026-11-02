package org.example.walkingrecommender.core.db.impl

import org.example.walkingrecommender.core.ab.Choice
import org.example.walkingrecommender.core.ab.Comparison
import org.example.walkingrecommender.core.db.ComparisonRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JdbcComparisonRepository(
    private val jdbcTemplate: JdbcTemplate,
) : ComparisonRepository {

    override fun save(comparison: Comparison) {
        jdbcTemplate.update(
            """
                INSERT INTO comparison (
                    id,
                    user_id,
                    preferences_version,
                    route_a_id,
                    route_b_id,
                    algorithm_version
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
            """.trimIndent(),
            comparison.id,
            comparison.userId,
            comparison.preferencesVersion,
            comparison.routeA.id,
            comparison.routeB.id,
            comparison.algorithmVersion,
        )
    }

    override fun update(comparisonId: UUID, choice: Choice) {
        jdbcTemplate.update(
            """
                UPDATE comparison
                SET choice = ?,
                    answered_at = now()
                WHERE id = ?
            """.trimIndent(),
            choice.toString(),
            comparisonId,
        )
    }
}