package org.example.walkingrecommender.core.db.impl

import org.example.walkingrecommender.core.db.GraphRepository
import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.EdgeSegment
import org.example.walkingrecommender.core.model.Graph
import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.model.SegmentFeatures
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKBReader
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.sql.Types

data class SegmentsWithEdges(
    val edges: Map<Long, Edge>,
    val segments: List<EdgeSegment>,
)

data class NearestNode(
    val node: Node,
    val distanceMeters: Double,
)

@Repository
class JdbcGraphRepository(
    private val edgeSegmentBuilder: EdgeSegmentBuilder,
    private val jdbcTemplate: JdbcTemplate,
) : GraphRepository {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun save(graph: Graph) {
        saveNodes(graph.nodes.values)

        logger.info("nodes saved")

        graph.edges
            .chunked(10_000)
            .forEachIndexed { batchIndex, edges ->
                logger.info("batch: $batchIndex")

                val ids = allocateEdgeIds(edges.size)

                logger.info("edge ids range: [${ids.first()}, ${ids.last()}]")

                val persistedEdges = edges
                    .zip(ids)
                    .map { (edge, id) -> edge.copy(id = id) }

                saveEdges(persistedEdges)

                logger.info("edges saved")

                val segments = persistedEdges.flatMap {
                    edgeSegmentBuilder.build(it)
                }

                logger.info("segments: ${segments.size}")

                saveSegments(segments)

                logger.info("segments saved")
            }
    }

    private fun saveNodes(nodes: Collection<Node>) {
        jdbcTemplate.batchUpdate(
            """
                INSERT INTO node(
                    node_osm_id,
                    geometry
                ) VALUES (
                    ?,
                    ST_GeomFromText(?, 4326)
                )
            """.trimIndent(),
            nodes,
            1000
        ) { ps, node ->
            ps.setLong(1, node.nodeOsmId)
            ps.setString(2, node.geometry.toText())
        }
    }

    private fun allocateEdgeIds(count: Int): List<Long> {
        return jdbcTemplate.queryForList(
            """
                SELECT nextval(pg_get_serial_sequence('edge', 'id'))
                FROM generate_series(1, ?)
            """.trimIndent(),
            Long::class.java,
            count,
        ).map { requireNotNull(it) }
    }

    private fun saveEdges(edges: Collection<Edge>) {
        jdbcTemplate.batchUpdate(
            """
                INSERT INTO edge(
                    id,
                    node_a_osm_id,
                    node_b_osm_id,
                    way_osm_id,
                    geometry,
                    highway,
                    surface,
                    name,
                    lanes,
                    lit,
                    distance,
                    walking_time
                ) VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ST_GeomFromText(?, 4326),
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
            """.trimIndent(),
            edges,
            1000
        ) { ps, edge ->
            ps.setLong(1, edge.id)
            ps.setLong(2, edge.nodeAOsmId)
            ps.setLong(3, edge.nodeBOsmId)
            ps.setLong(4, edge.wayOsmId)
            ps.setString(5, edge.geometry.toText())
            ps.setString(6, edge.highway)
            ps.setString(7, edge.surface)
            ps.setString(8, edge.name)
            ps.setObject(9, edge.lanes, Types.INTEGER)
            ps.setBoolean(10, edge.lit)
            ps.setInt(11, edge.distance)
            ps.setInt(12, edge.walkingTime)
        }
    }

    private fun saveSegments(segments: List<EdgeSegment>) {
        jdbcTemplate.batchUpdate(
            """
                INSERT INTO edge_segment(
                    edge_id,
                    segment_index,
                    geometry,
                    distance_meters
                )
                VALUES (
                    ?,
                    ?,
                    ST_GeomFromText(?, 4326),
                    ?
                )
            """.trimIndent(),
            segments,
            1000
        ) { ps, segment ->
            ps.setLong(1, segment.edgeId)
            ps.setInt(2, segment.index)
            ps.setString(3, segment.geometry.toText())
            ps.setDouble(4, segment.distanceMeters)
        }
    }

    override fun updateSegmentsFeatures(segments: List<EdgeSegment>) {
        jdbcTemplate.batchUpdate(
            """
                UPDATE edge_segment
                SET
                    greenery = ?,
                    water = ?,
                    traffic = ?,
                    noise = ?,
                    historic_architecture = ?,
                    modern_architecture = ?,
                    commercial_activity = ?,
                    grittiness = ?,
                    pedestrian_comfort = ?,
                    features_version = ?
                WHERE edge_id = ?
                  AND segment_index = ?
            """.trimIndent(),
            segments,
            1000,
        ) { ps, segment ->
            val features = requireNotNull(segment.features) {
                "Features must be calculated before saving"
            }

            val version = requireNotNull(segment.featuresVersion) {
                "Features version must be set before saving"
            }

            ps.setDouble(1, features.greenery)
            ps.setDouble(2, features.water)
            ps.setDouble(3, features.traffic)
            ps.setDouble(4, features.noise)
            ps.setDouble(5, features.historicArchitecture)
            ps.setDouble(6, features.modernArchitecture)
            ps.setDouble(7, features.commercialActivity)
            ps.setDouble(8, features.grittiness)
            ps.setDouble(9, features.pedestrianComfort)

            ps.setInt(10, version)

            ps.setLong(11, segment.edgeId)
            ps.setInt(12, segment.index)
        }
    }

    override fun findSegmentsWithVersionLessThan(version: Int, limit: Int): SegmentsWithEdges {
        val edges = mutableMapOf<Long, Edge>()
        val wkbReader = WKBReader()

        val segments = jdbcTemplate.query(
            """
                SELECT s.edge_id,
                       s.segment_index,
                       ST_AsEWKB(s.geometry) as segment_geometry,
                       s.distance_meters,
                       s.greenery,
                       s.water,
                       s.traffic,
                       s.noise,
                       s.historic_architecture,
                       s.modern_architecture,
                       s.commercial_activity,
                       s.grittiness,
                       s.pedestrian_comfort,
                       s.features_version,
                       
                       e.node_a_osm_id,
                       e.node_b_osm_id,
                       e.way_osm_id,
                       ST_AsEWKB(e.geometry) as edge_geometry,
                       e.highway,
                       e.surface,
                       e.name,
                       e.lanes,
                       e.lit,
                       e.distance,
                       e.walking_time
                       
                FROM edge_segment s
                JOIN edge e ON e.id = s.edge_id
                
                WHERE s.features_version IS NULL
                   OR s.features_version < ?
                   
                LIMIT ?
            """.trimIndent(),
            { rs, _ ->
                val edgeId = rs.getLong("edge_id")

                if (edgeId !in edges) {
                    edges[edgeId] = mapEdge(rs, wkbReader)
                }

                mapEdgeSegment(rs, wkbReader)
            },
            version, limit,
        )

        return SegmentsWithEdges(
            edges = edges,
            segments = segments,
        )
    }

    override fun findNearestNode(point: Point): NearestNode? {
        val wkbReader = WKBReader()

        return jdbcTemplate.query(
            """
                WITH p AS (
                    SELECT ST_SetSRID(
                        ST_MakePoint(?, ?),
                        4326
                    ) AS geometry
                )
                SELECT
                    n.id,
                    n.node_osm_id,
                    ST_AsEWKB(n.geometry) AS geometry,

                    ST_Distance(
                        n.geometry::geography,
                        p.geometry::geography
                    ) AS distance_meters

                FROM node n
                CROSS JOIN p

                ORDER BY n.geometry <-> p.geometry
                LIMIT 1
            """.trimIndent(),
            { rs, _ ->
                NearestNode(
                    node = mapNode(rs, wkbReader),
                    distanceMeters = rs.getDouble("distance_meters"),
                )
            },
            point.x, point.y,
        ).firstOrNull()
    }

    override fun findSubgraph(center: Point, radiusMeters: Double): Graph {
        val wkbReader = WKBReader()

        val edges = findEdgesWithSegmentsWithin(center, radiusMeters, wkbReader)

        if (edges.isEmpty()) {
            return Graph(
                nodes = emptyMap(),
                edges = emptyList(),
            )
        }

        val nodeOsmIds = edges
            .flatMap { listOf(it.nodeAOsmId, it.nodeBOsmId) }
            .distinct()

        val nodes = nodeOsmIds
            .chunked(10_000)
            .flatMap { batch -> findNodesByOsmIds(batch, wkbReader) }

        return Graph(
            nodes = nodes.associateBy { it.nodeOsmId },
            edges = edges,
        )
    }

    private fun findEdgesWithSegmentsWithin(
        center: Point,
        radiusMeters: Double,
        wkbReader: WKBReader,
    ): List<Edge> {
        data class Accumulator(
            val edge: Edge,
            val segments: MutableList<EdgeSegment>
        )

        val edges = mutableMapOf<Long, Accumulator>()

        jdbcTemplate.query(
            """
                SELECT e.id as edge_id,
                       e.node_a_osm_id,
                       e.node_b_osm_id,
                       e.way_osm_id,
                       ST_AsEWKB(e.geometry) as edge_geometry,
                       e.highway,
                       e.surface,
                       e.name,
                       e.lanes,
                       e.lit,
                       e.distance,
                       e.walking_time,
                       
                       s.segment_index,
                       ST_AsEWKB(s.geometry) AS segment_geometry,
                       s.distance_meters,
                       s.greenery,
                       s.water,
                       s.traffic,
                       s.noise,
                       s.historic_architecture,
                       s.modern_architecture,
                       s.commercial_activity,
                       s.grittiness,
                       s.pedestrian_comfort,
                       s.features_version
                    
                FROM edge e
                
                LEFT JOIN edge_segment s ON s.edge_id = e.id
                
                WHERE ST_DWithin(
                    e.geometry::geography,
                    ST_SetSRID(
                        ST_MakePoint(?, ?),
                        4326
                    )::geography,
                    ?
                )
                
                ORDER BY e.id, s.segment_index
            """.trimIndent(),
            { rs, _ ->
                val edgeId = rs.getLong("edge_id")

                val acc = edges.getOrPut(edgeId) {
                    Accumulator(
                        mapEdge(rs, wkbReader),
                        mutableListOf(),
                    )
                }

                acc.segments += mapEdgeSegment(rs, wkbReader)
            },
            center.x, center.y, radiusMeters,
        )

        return edges.values.map { acc ->
            acc.edge.copy(
                segments = acc.segments,
            )
        }
    }

    private fun findNodesByOsmIds(osmIds: List<Long>, wkbReader: WKBReader): List<Node> {
        if (osmIds.isEmpty()) {
            return emptyList()
        }

        val placeholders = osmIds.joinToString(",") { "?" }

        return jdbcTemplate.query(
            """
                SELECT
                    n.id,
                    n.node_osm_id,
                    ST_AsEWKB(geometry) AS geometry
                FROM node n
                WHERE n.node_osm_id IN ($placeholders)
            """.trimIndent(),
            { rs, _ ->
                mapNode(rs, wkbReader)
            },
            *osmIds.toTypedArray()
        )
    }

    private fun mapNode(rs: ResultSet, wkbReader: WKBReader): Node =
        Node(
            id = rs.getLong("id"),
            nodeOsmId = rs.getLong("node_osm_id"),
            geometry = wkbReader.read(
                rs.getBytes("geometry")
            ) as Point,
        )

    private fun mapEdge(rs: ResultSet, wkbReader: WKBReader): Edge =
        Edge(
            id = rs.getLong("edge_id"),
            nodeAOsmId = rs.getLong("node_a_osm_id"),
            nodeBOsmId = rs.getLong("node_b_osm_id"),
            wayOsmId = rs.getLong("way_osm_id"),
            geometry = wkbReader.read(
                rs.getBytes("edge_geometry")
            ) as LineString,
            highway = rs.getString("highway"),
            surface = rs.getString("surface"),
            name = rs.getString("name"),
            lanes = rs.getObject("lanes", Int::class.javaObjectType),
            lit = rs.getBoolean("lit"),
            distance = rs.getInt("distance"),
            walkingTime = rs.getInt("walking_time")
        )

    private fun mapEdgeSegment(rs: ResultSet, wkbReader: WKBReader): EdgeSegment {
        val featuresVersion = rs.getObject("features_version", Int::class.javaObjectType)

        val features = if (featuresVersion == null) {
            null
        } else {
            SegmentFeatures(
                greenery = rs.getDouble("greenery"),
                water = rs.getDouble("water"),
                traffic = rs.getDouble("traffic"),
                noise = rs.getDouble("noise"),
                historicArchitecture = rs.getDouble("historic_architecture"),
                modernArchitecture = rs.getDouble("modern_architecture"),
                commercialActivity = rs.getDouble("commercial_activity"),
                grittiness = rs.getDouble("grittiness"),
                pedestrianComfort = rs.getDouble("pedestrian_comfort"),
            )
        }

        return EdgeSegment(
            edgeId = rs.getLong("edge_id"),
            index = rs.getInt("segment_index"),
            geometry = wkbReader.read(
                rs.getBytes("segment_geometry")
            ) as LineString,
            distanceMeters = rs.getDouble("distance_meters"),
            features = features,
            featuresVersion = featuresVersion,
        )
    }
}