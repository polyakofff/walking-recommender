package org.example.walkingrecommender.core.db.impl

import org.example.walkingrecommender.core.db.RouteRepository
import org.example.walkingrecommender.core.model.DbRoute
import org.example.walkingrecommender.core.model.RouteType
import org.example.walkingrecommender.core.model.toDbRoute
import org.example.walkingrecommender.core.routing.ScoredRoute
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class JdbcRouteRepository(
    private val geometryFactory: GeometryFactory,
    private val jdbcTemplate: JdbcTemplate,
) : RouteRepository {

    override fun save(route: ScoredRoute, type: RouteType): DbRoute {
        val dbRoute = route.toDbRoute(type, geometryFactory)

        jdbcTemplate.update(
            """
                INSERT INTO route (
                    id,
                    type,
                    geometry,
                    distance,
                    duration,
                    attractiveness,
                    score
                )
                VALUES (
                    ?,
                    ?,
                    ST_GeomFromText(?, 4326),
                    ?,
                    ?,
                    ?,
                    ?
                )
            """.trimIndent(),
            dbRoute.id,
            dbRoute.type.toString(),
            dbRoute.geometry.toText(),
            dbRoute.distance,
            dbRoute.duration,
            dbRoute.attractiveness,
            dbRoute.score,
        )

        return dbRoute
    }
}