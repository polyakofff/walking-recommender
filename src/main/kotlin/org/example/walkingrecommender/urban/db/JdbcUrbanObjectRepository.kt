package org.example.walkingrecommender.urban.db

import org.example.walkingrecommender.urban.OsmElementType
import org.example.walkingrecommender.urban.SegmentId
import org.example.walkingrecommender.urban.UrbanObject
import org.example.walkingrecommender.urban.UrbanObjectType
import org.locationtech.jts.io.WKBReader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

data class NearbyUrbanObject(
    val id: Long,
    val osmElementType: OsmElementType,
    val osmId: Long,
    val urbanObjectType: UrbanObjectType,
    val name: String?,
    val tags: Map<String, String>,
    val distance: Double,
)

@Repository
class JdbcUrbanObjectRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) : UrbanObjectRepository {

    private val wkbReader = WKBReader()

    @Transactional
    override fun save(objects: List<UrbanObject>) {
        saveObjects(objects)
    }

    private fun saveObjects(objects: Collection<UrbanObject>) {
        jdbcTemplate.batchUpdate(
            """
                INSERT INTO urban_object(
                    osm_element_type,
                    osm_id,
                    urban_object_type,
                    name,
                    osm_tags,
                    geometry
                ) VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?::jsonb,
                    ST_GeomFromText(?, 4326)
                )
            """.trimIndent(),
            objects,
            1000
        ) { ps, obj ->
            ps.setString(1, obj.osmElementType.toString())
            ps.setLong(2, obj.osmId)
            ps.setString(3, obj.urbanObjectType.toString())
            ps.setString(4, obj.name)
            ps.setString(5, objectMapper.writeValueAsString(obj.tags))
            ps.setString(6, obj.geometry.toText())
        }
    }

    override fun findWithinObjects(
        segmentIds: List<SegmentId>,
        radiusMeters: Double,
    ): Map<SegmentId, List<NearbyUrbanObject>> {
        if (segmentIds.isEmpty()) {
            return emptyMap()
        }

        val values = segmentIds.joinToString(",") { "(?, ?)" }

        val args = buildList<Any> {
            segmentIds.forEach {
                add(it.edgeId)
                add(it.segmentIndex)
            }
            add(radiusMeters)
        }

        val found = jdbcTemplate.query(
            """
                WITH requested_segments(edge_id, segment_index) AS (
                    VALUES $values
                )
                SELECT 
                    s.edge_id as edge_id,
                    s.segment_index as segment_index,
                    
                    o.id as obj_id,
                    o.osm_element_type,
                    o.osm_id,
                    o.urban_object_type,
                    o.name,
                    o.osm_tags,
                    
                    ST_Distance(
                        s.geometry::geography,
                        o.geometry::geography
                    ) as distance_meters
                    
                FROM requested_segments r
                
                JOIN edge_segment s
                    ON s.edge_id = r.edge_id
                    AND s.segment_index = r.segment_index
                
                JOIN urban_object o
                    ON ST_DWithin(
                        s.geometry::geography,
                        o.geometry::geography,
                        ?
                    )
                ;
            """.trimIndent(),
            { rs, _ ->
                val segmentId = SegmentId(
                    edgeId = rs.getLong("edge_id"),
                    segmentIndex = rs.getInt("segment_index")
                )
                val nearbyObject = NearbyUrbanObject(
                    id = rs.getLong("obj_id"),
                    osmElementType = OsmElementType.valueOf(rs.getString("osm_element_type")),
                    osmId = rs.getLong("osm_id"),
                    urbanObjectType = UrbanObjectType.valueOf(rs.getString("urban_object_type")),
                    name = rs.getString("name"),
                    tags = objectMapper.readValue(
                        rs.getString("osm_tags"),
                        object : TypeReference<Map<String, String>>() {}
                    ),
                    distance = rs.getDouble("distance_meters")
                )
                segmentId to nearbyObject
            },
            *args.toTypedArray()
        )

        val grouped = found
            .groupBy { it.first }
            .mapValues { (_, rows) ->
                rows.map { it.second }
            }

        return segmentIds.associateWith { id -> grouped[id].orEmpty() }
    }
}