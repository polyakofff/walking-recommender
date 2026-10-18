package org.example.walkingrecommender.core.evaluation

import org.example.walkingrecommender.api.dto.EvaluateRequest
import org.example.walkingrecommender.api.dto.EvaluationRequest
import org.example.walkingrecommender.core.db.EvaluationRepository
import org.example.walkingrecommender.core.db.RouteRepository
import org.example.walkingrecommender.core.model.RouteType
import org.example.walkingrecommender.core.routing.RouteService
import org.example.walkingrecommender.user.UserService
import org.example.walkingrecommender.user.toPreferences
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EvaluationService(
    private val userService: UserService,
    private val routeService: RouteService,
    private val routeRepository: RouteRepository,
    private val evaluationRepository: EvaluationRepository,
) {

    fun getEvaluations(request: EvaluationRequest): List<Evaluation> {
        val user = userService.get(request.username)
            ?: error("User ${request.username} not found")

        if (user.preferences == null) {
            error("User ${request.username} not configured")
        }

        val durationSeconds = request.durationMinutes * 60

        val preferences = user.preferences.toPreferences()

        val (routes, _) = routeService.getBestNRoutes(
            startLon = request.start.longitude,
            startLat = request.start.latitude,
            endLon = request.end.longitude,
            endLat = request.end.latitude,
            durationSeconds = durationSeconds,
            preferences = preferences,
            n = request.count,
        )

        val evaluations = routes.shuffled().map { route ->
            val dbRoute = routeRepository.save(
                route = route,
                type = RouteType.PERSONALIZED_SCENIC,
            )

            val evaluationId = UUID.randomUUID()

            val evaluation = Evaluation(
                id = evaluationId,
                userId = user.id,
                preferencesVersion = user.preferences.version,
                route = dbRoute,
                algorithmVersion = routeService.algorithmVersion(),
            )

            evaluationRepository.save(evaluation)

            evaluation
        }

        return evaluations
    }

    fun evaluate(request: EvaluateRequest) {
        println("${request.rating}/$MAX_RATING")
        println("reasons: ${request.reasons}")
        println("other: ${request.other}")

        val reasons = (request.reasons.map { it.name } + request.other)
            .filterNotNull()
            .joinToString()

        evaluationRepository.update(
            evaluationId = request.evaluationId,
            rating = request.rating,
            reasons = reasons,
        )
    }
}