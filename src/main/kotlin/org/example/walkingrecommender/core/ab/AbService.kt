package org.example.walkingrecommender.core.ab

import org.example.walkingrecommender.api.dto.AbRequest
import org.example.walkingrecommender.api.dto.VoteRequest
import org.example.walkingrecommender.core.db.ComparisonRepository
import org.example.walkingrecommender.core.db.RouteRepository
import org.example.walkingrecommender.core.model.RouteType
import org.example.walkingrecommender.core.routing.*
import org.example.walkingrecommender.user.UserService
import org.example.walkingrecommender.user.toPreferences
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Service
class AbService(
    private val userService: UserService,
    private val routeService: RouteService,
    private val routeRepository: RouteRepository,
    private val comparisonRepository: ComparisonRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val labels = ConcurrentHashMap<UUID, String>()

    fun getComparisons(request: AbRequest): List<Comparison> {
        val user = userService.get(request.username)
            ?: error("User ${request.username} not found")

        if (user.preferences == null) {
            error("User ${request.username} not configured")
        }

        val durationSeconds = request.durationMinutes * 60

        val preferences = user.preferences.toPreferences()

        val (routes, graph) = routeService.getBestNRoutes(
            startLon = request.start.longitude,
            startLat = request.start.latitude,
            endLon = request.end.longitude,
            endLat = request.end.latitude,
            durationSeconds = durationSeconds,
            preferences = preferences,
            n = request.count,
        )

        val comparisons = routes.shuffled().map { route ->
            val paired = routeService.getPairedFastestRoute(route, graph)

            val dbRoute = routeRepository.save(
                route = route,
                type = RouteType.PERSONALIZED_SCENIC,
            )
            val dbPaired = routeRepository.save(
                route = paired,
                type = RouteType.FASTEST,
            )

            val comparisonId = UUID.randomUUID()

            val (routeA, routeB) = if (Random.nextBoolean()) { // swap
                labels[comparisonId] = "B"
                dbPaired to dbRoute
            } else {
                labels[comparisonId] = "A"
                dbRoute to dbPaired
            }

            val comparison = Comparison(
                id = comparisonId,
                userId = user.id,
                preferencesVersion = user.preferences.version,
                routeA = routeA,
                routeB = routeB,
                algorithmVersion = routeService.algorithmVersion(),
            )
            comparisonRepository.save(comparison)

            comparison
        }

        return comparisons
    }

    fun vote(request: VoteRequest) {
        val label = labels[request.comparisonId]
            ?: error("Unknown comparison [${request.comparisonId}]")

        val result = when {
            request.choice == Choice.EQUAL -> "Equal"
            request.choice == Choice.NONE -> "None"
            request.choice.name == label -> "${request.choice.name} -> Right :)"
            request.choice.name != label -> "${request.choice.name} -> Wrong :("
            else -> error("Unknown choice")
        }
        logger.info("Comparison ${request.comparisonId}: $result")

        comparisonRepository.update(
            comparisonId = request.comparisonId,
            choice = request.choice
        )
    }
}