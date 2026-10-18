package org.example.walkingrecommender.api.dto

import org.example.walkingrecommender.user.Feature
import org.example.walkingrecommender.user.PreferenceAnswer
import org.example.walkingrecommender.user.PreferenceQuestion

data class PreferenceQuestionDto(
    val feature: Feature,
    val text: String,
)

fun PreferenceQuestion.toDto(): PreferenceQuestionDto {
    return PreferenceQuestionDto(
        feature = this.feature,
        text = this.text,
    )
}

data class PreferenceAnswerDto(
    val value: PreferenceAnswer,
    val text: String,
)

fun PreferenceAnswer.toDto(): PreferenceAnswerDto {
    return PreferenceAnswerDto(
        value = this,
        text = this.label,
    )
}

data class SetupDto(
    val questions: List<PreferenceQuestionDto>,
    val answers: List<PreferenceAnswerDto>,
)