package org.example.walkingrecommender.api

import org.example.walkingrecommender.MemoryUtils
import org.example.walkingrecommender.api.dto.*
import org.example.walkingrecommender.core.evaluation.EvaluationService
import org.example.walkingrecommender.core.evaluation.MAX_RATING
import org.example.walkingrecommender.core.evaluation.ReasonItems
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/evaluation")
class EvaluationController(
    private val evaluationService: EvaluationService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/generate")
    fun generate(@RequestBody request: EvaluationRequest): EvaluationResponse {
        logger.info("generate: {}", request)

        val evaluations = evaluationService.getEvaluations(request)
            .map {
                Evaluation(
                    id = it.id,
                    route = it.route.toDto(),
                )
            }

        logger.info(
            "heapUsed={} MB, heapMax={} MB",
            MemoryUtils.heapUsedMb(),
            MemoryUtils.heapMaxMb(),
        )

        return EvaluationResponse(
            maxRating = MAX_RATING,
            reasons = ReasonItems.reasons.map { it.toDto() },
            evaluations = evaluations,
        )
    }

    @PostMapping("/evaluate")
    fun evaluate(@RequestBody request: EvaluateRequest) {
        logger.info("evaluate: {}", request)

        evaluationService.evaluate(request)
    }
}