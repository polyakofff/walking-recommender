package org.example.walkingrecommender.user.db

import org.example.walkingrecommender.user.UserPreferences
import java.util.UUID

interface UserPreferencesRepository {

    fun findLatest(userId: UUID): UserPreferences?

    fun save(userId: UUID, userPreferences: UserPreferences)
}