package org.example.walkingrecommender.user

import org.example.walkingrecommender.user.db.UserPreferencesRepository
import org.example.walkingrecommender.user.db.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    @Qualifier("jdbcUserRepository") private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    fun getOrCreate(username: String): User {
        return userRepository.find(username)
            ?: userRepository.save(
                User(
                    id = UUID.randomUUID(),
                    username = username,
                )
            )
    }

    fun get(username: String): User? {
        return userRepository.find(username)
    }

    @Transactional
    fun savePreferences(username: String, answers: Map<Feature, PreferenceAnswer>): User {
        val missingFeatures = Feature.entries.toSet() - answers.keys
        require(missingFeatures.isEmpty()) {
            "Missing answers for $missingFeatures"
        }

        val user = getOrCreate(username)

        val latestVersion = user.preferences?.version ?: 0

        val preferences = UserPreferences(
            weights = answers.mapValues { it.value.weight },
            version = latestVersion + 1,
        )

        userPreferencesRepository.save(user.id, preferences)

        return user.copy(
            preferences = preferences
        )
    }
}