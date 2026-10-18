package org.example.walkingrecommender.api

import org.example.walkingrecommender.api.dto.SetupDto
import org.example.walkingrecommender.api.dto.toDto
import org.example.walkingrecommender.user.PreferenceAnswer
import org.example.walkingrecommender.user.PreferenceQuestions
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/setup")
class SetupController {

    @GetMapping
    fun getSetup(): SetupDto {
        println("get setup")

        return SetupDto(
            questions = PreferenceQuestions.questions.map { it.toDto() },
            answers = PreferenceAnswer.entries.map { it.toDto() },
        )
    }
}