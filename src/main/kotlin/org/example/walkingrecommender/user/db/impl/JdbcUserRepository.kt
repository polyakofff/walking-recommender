package org.example.walkingrecommender.user.db.impl

import org.example.walkingrecommender.user.Feature
import org.example.walkingrecommender.user.User
import org.example.walkingrecommender.user.UserPreferences
import org.example.walkingrecommender.user.db.UserRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Repository
class JdbcUserRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) : UserRepository {

    override fun find(username: String): User? {
        return jdbcTemplate.query(
            """
                SELECT u.id,
                       u.username,
                       p.version,
                       p.weights
                FROM app_user u
                LEFT JOIN user_preferences p ON p.user_id = u.id
                WHERE u.username = ?
                ORDER BY p.version DESC
                LIMIT 1
            """.trimIndent(),
            { rs, _ ->
                val version = rs.getInt("version")
                User(
                    id = UUID.fromString(rs.getString("id")),
                    username = rs.getString("username"),
                    preferences = if (version != 0) {
                        UserPreferences(
                            weights = objectMapper.readValue(
                                rs.getString("weights"),
                                object : TypeReference<Map<Feature, Double>>() {}
                            ),
                            version = version,
                        )
                    } else {
                        null
                    }
                )
            },
            username
        ).firstOrNull()
    }

    override fun save(user: User): User {
        jdbcTemplate.update(
            """
                INSERT INTO app_user (
                    id,
                    username
                )
                VALUES (?, ?)
        """.trimIndent(),
            user.id,
            user.username,
        )
        return user
    }
}