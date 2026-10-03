package org.example.walkingrecommender.api

import org.example.walkingrecommender.core.ab.AbService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ab")
class AbController(
    private val abService: AbService,
) {

    @PostMapping("/generate")
    fun generate(@RequestBody request: AbRequest): AbResponse {
        println(request)

        val comparisons = abService.getComparisons(request)
            .map {
                Comparison(
                    id = it.id,
                    routeA = it.routeA.toDto(),
                    routeB = it.routeB.toDto(),
                )
            }

        return AbResponse(
            comparisons = comparisons,
        )
    }

    @PostMapping("/vote")
    fun vote(@RequestBody request: VoteRequest) {
        println(request)

        abService.vote(request)
    }
}