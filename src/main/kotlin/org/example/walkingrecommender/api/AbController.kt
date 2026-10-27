package org.example.walkingrecommender.api

import org.example.walkingrecommender.MemoryUtils
import org.example.walkingrecommender.api.dto.AbRequest
import org.example.walkingrecommender.api.dto.AbResponse
import org.example.walkingrecommender.api.dto.Comparison
import org.example.walkingrecommender.api.dto.VoteRequest
import org.example.walkingrecommender.api.dto.toDto
import org.example.walkingrecommender.core.ab.AbService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ab")
class AbController(
    private val abService: AbService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/generate")
    fun generate(@RequestBody request: AbRequest): AbResponse {
        logger.info("generate: {}", request)

        val comparisons = abService.getComparisons(request)
            .map {
                Comparison(
                    id = it.id,
                    routeA = it.routeA.toDto(),
                    routeB = it.routeB.toDto(),
                )
            }

        logger.info(
            "heapUsed={} MB, heapMax={} MB",
            MemoryUtils.heapUsedMb(),
            MemoryUtils.heapMaxMb(),
        )

        return AbResponse(
            comparisons = comparisons,
        )
    }

    @PostMapping("/vote")
    fun vote(@RequestBody request: VoteRequest) {
        logger.info("vote: {}", request)

        abService.vote(request)
    }
}