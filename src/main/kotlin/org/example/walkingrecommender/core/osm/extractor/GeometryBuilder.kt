package org.example.walkingrecommender.core.osm.extractor

import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.OsmNodeDto
import org.example.walkingrecommender.core.osm.OsmRelationDto
import org.example.walkingrecommender.core.osm.OsmWayDto
import org.locationtech.jts.geom.*
import org.springframework.stereotype.Component

data class PolygonParts(
    val shell: LinearRing,
    val holes: MutableList<LinearRing> = mutableListOf(),
)

@Component
class GeometryBuilder(private val geometryFactory: GeometryFactory) {

    fun buildPoint(node: OsmNodeDto): Point {
        return geometryFactory.createPoint(
            Coordinate(node.lon, node.lat),
        )
    }

    fun buildLineString(way: OsmWayDto, data: OsmData): LineString {
        return geometryFactory.createLineString(
            way.nodeIds.map { nodeId ->
                val node = data.nodesMap[nodeId]!!
                Coordinate(node.lon, node.lat)
            }.toTypedArray()
        )
    }

    fun buildPolygon(way: OsmWayDto, data: OsmData): Polygon {
        if (way.nodeIds.first() != way.nodeIds.last()) {
            error("Way first and last nodes must be same for polygon")
        }
        return geometryFactory.createPolygon(
            buildLinearRing(way.nodeIds, data.nodesMap)
        )
    }

    fun buildMultipolygon(relation: OsmRelationDto, data: OsmData): MultiPolygon {
        val outerWays = relation.members.filter { !it.inner }.mapNotNull { data.waysMap[it.wayId] }
        val innerWays = relation.members.filter { it.inner }.mapNotNull { data.waysMap[it.wayId] }

        val outerRings = buildRings(outerWays)
        val innerRings = buildRings(innerWays)

        val polygonParts = outerRings.map { nodeIds ->
            PolygonParts(
                shell = buildLinearRing(nodeIds, data.nodesMap)
            )
        }

        val innerLinearRings = innerRings.map { nodeIds -> buildLinearRing(nodeIds, data.nodesMap) }

        for (inner in innerLinearRings) {
            val innerPolygon = geometryFactory.createPolygon(inner)

            polygonParts.filter {
                val outerPolygon = geometryFactory.createPolygon(it.shell)
                outerPolygon.covers(innerPolygon)
            }.minByOrNull {
                val outerPolygon = geometryFactory.createPolygon(it.shell)
                outerPolygon.area
            }?.let {
                it.holes += inner
            }
        }

        val polygons = polygonParts.map {
            geometryFactory.createPolygon(
                it.shell,
                it.holes.toTypedArray()
            )
        }

        return geometryFactory.createMultiPolygon(
            polygons.toTypedArray()
        )
    }

    private fun buildRings(ways: List<OsmWayDto>): List<List<Long>> {
        val rings = mutableListOf<List<Long>>()
        val unused = ways.toMutableList()

        while (unused.isNotEmpty()) {
            val way0 = unused.removeAt(0)
            val ring = way0.nodeIds.toMutableList()
            while (ring.first() != ring.last()) {
                val last = ring.last()
                val index = unused.indexOfFirst { way ->
                    way.nodeIds.first() == last || way.nodeIds.last() == last
                }
                if (index == -1) {
                    println("Cannot close ring for way [${way0.id}]")
                    break
                }
                val way = unused.removeAt(index)
                val newNodes = if (way.nodeIds.first() == last) {
                    way.nodeIds
                } else {
                    way.nodeIds.reversed()
                }
                ring += newNodes.drop(1)
            }
            if (ring.first() == ring.last()) {
                rings += ring
            }
        }

        return rings
    }

    private fun buildLinearRing(nodeIds: List<Long>, nodesMap: Map<Long, OsmNodeDto>): LinearRing {
        return geometryFactory.createLinearRing(
            nodeIds.map {
                val node = nodesMap[it]!!
                Coordinate(node.lon, node.lat)
            }.toTypedArray()
        )
    }
}