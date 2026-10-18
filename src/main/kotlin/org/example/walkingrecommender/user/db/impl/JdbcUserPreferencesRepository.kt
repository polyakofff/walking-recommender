package org.example.walkingrecommender.user.db.impl

import org.example.walkingrecommender.user.Feature
import org.example.walkingrecommender.user.UserPreferences
import org.example.walkingrecommender.user.db.UserPreferencesRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Repository
class JdbcUserPreferencesRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) : UserPreferencesRepository {

    override fun findLatest(userId: UUID): UserPreferences? {
        return jdbcTemplate.query(
            """
                SELECT p.weights,
                       p.version
                FROM user_preferences p
                WHERE p.user_id = ?
                ORDER BY p.version DESC
                LIMIT 1
            """.trimIndent(),
            { rs, _ ->
                UserPreferences(
                    weights = objectMapper.readValue(
                        rs.getString("weights"),
                        object : TypeReference<Map<Feature, Double>>() {}
                    ),
                    version = rs.getInt("version"),
                )
            },
            userId,
        ).firstOrNull()
    }

    override fun save(userId: UUID, userPreferences: UserPreferences) {
        val weightsJson = objectMapper.writeValueAsString(
            userPreferences.weights
        )

        jdbcTemplate.update(
            """
                INSERT INTO user_preferences (
                    user_id,
                    version,
                    weights
                )
                VALUES (?, ?, ?::jsonb)
        """.trimIndent(),
            userId,
            userPreferences.version,
            weightsJson,
        )
    }
}