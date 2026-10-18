package org.example.walkingrecommender.api

import org.example.walkingrecommender.api.dto.*
import org.example.walkingrecommender.core.evaluation.EvaluationService
import org.example.walkingrecommender.core.evaluation.MAX_RATING
import org.example.walkingrecommender.core.evaluation.ReasonItems
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/evaluation")
class EvaluationController(
    private val evaluationService: EvaluationService,
) {

    @PostMapping("/generate")
    fun generate(@RequestBody request: EvaluationRequest): EvaluationResponse {
        println("generate: $request")

        val evaluations = evaluationService.getEvaluations(request)
            .map {
                Evaluation(
                    id = it.id,
                    route = it.route.toDto(),
                )
            }

        return EvaluationResponse(
            maxRating = MAX_RATING,
            reasons = ReasonItems.reasons,
            evaluations = evaluations,
        )
    }

    @PostMapping("/evaluate")
    fun evaluate(@RequestBody request: EvaluateRequest) {
        println("evaluate: $request")

        evaluationService.evaluate(request)
    }
}