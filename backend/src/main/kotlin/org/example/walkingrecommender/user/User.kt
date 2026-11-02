package org.example.walkingrecommender.user

import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
    val preferences: UserPreferences? = null,
)

data class UserPreferences(
    val weights: Map<Feature, Double>,
    val version: Int,
)

enum class Feature {
    GREENERY,
    WATER,
    TRAFFIC,
    NOISE,
    HISTORIC_ARCHITECTURE,
    MODERN_ARCHITECTURE,
    COMMERCIAL_ACTIVITY,
    GRITTINESS,
    PEDESTRIAN_COMFORT,
}

fun UserPreferences.toPreferences(): Preferences {
    return Preferences(
        greenery = this.weights[Feature.GREENERY] ?: 0.0,
        water = this.weights[Feature.WATER] ?: 0.0,
        traffic = this.weights[Feature.TRAFFIC] ?: 0.0,
        noise = this.weights[Feature.NOISE] ?: 0.0,
        historicArchitecture = this.weights[Feature.HISTORIC_ARCHITECTURE] ?: 0.0,
        modernArchitecture = this.weights[Feature.MODERN_ARCHITECTURE] ?: 0.0,
        commercialActivity = this.weights[Feature.GREENERY] ?: 0.0,
        grittiness = this.weights[Feature.GRITTINESS] ?: 0.0,
        pedestrianComfort = this.weights[Feature.PEDESTRIAN_COMFORT] ?: 0.0,
    )
}

data class PreferenceQuestion(
    val feature: Feature,
    val text: String,
)

object PreferenceQuestions {
    val questions = listOf(
        PreferenceQuestion(
            Feature.GREENERY,
            "Как вы относитесь к улицам полным зелени?",
        ),
        PreferenceQuestion(
            Feature.WATER,
            "Как вы относитесь к улицам вдоль водоемов?",
        ),
        PreferenceQuestion(
            Feature.TRAFFIC,
            "Как вы относитесь к улицам с плотным автомобильным движением?",
        ),
        PreferenceQuestion(
            Feature.NOISE,
            "Как вы относитесь к шумным улицам?",
        ),
        PreferenceQuestion(
            Feature.HISTORIC_ARCHITECTURE,
            "Как вы относитесь к улицам с исторической архитектурой?",
        ),
        PreferenceQuestion(
            Feature.MODERN_ARCHITECTURE,
            "Как вы относитесь к улицам с современной архитектурой?",
        ),
        PreferenceQuestion(
            Feature.COMMERCIAL_ACTIVITY,
            "Как вы относитесь к оживленным улицам с кафе, магазинами и т.п.?",
        ),
        PreferenceQuestion(
            Feature.GRITTINESS,
            "Как вы относитесь к индустриальной среде: промзоны, ж/д пути, гаражи?",
        ),
        PreferenceQuestion(
            Feature.PEDESTRIAN_COMFORT,
            "Как вы относитесь к улицам с комфортной пешеходной инфраструктурой?",
        ),
    )
}

enum class PreferenceAnswer(val weight: Double, val label: String) {
    VERY_BAD(
        -1.0,
        "Очень плохо",
    ),
    BAD(
        -2.0 / 3.0,
        "Плохо",
    ),
    RATHER_BAD(
        -1.0 / 3.0,
        "Скорее плохо",
    ),
    NEUTRAL(
        0.0,
        "Нейтрально",
    ),
    RATHER_GOOD(
        1.0 / 3.0,
        "Скорее хорошо",
    ),
    GOOD(
        2.0 / 3.0,
        "Хорошо",
    ),
    VERY_GOOD(
        1.0,
        "Очень хорошо",
    ),
}