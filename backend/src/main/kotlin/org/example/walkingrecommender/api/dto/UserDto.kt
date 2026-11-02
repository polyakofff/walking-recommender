package org.example.walkingrecommender.api.dto

import org.example.walkingrecommender.user.Feature
import org.example.walkingrecommender.user.PreferenceAnswer
import org.example.walkingrecommender.user.User

data class CreateUserRequest(
    val username: String,
)

data class UserDto(
    val username: String,
    val configured: Boolean,
    val preferences: Map<Feature, Double>?,
)

fun User.toDto(): UserDto {
    return UserDto(
        username = username,
        configured = preferences != null,
        preferences = preferences?.weights,
    )
}

data class SavePreferencesRequest(
    val answers: Map<Feature, PreferenceAnswer>,
)