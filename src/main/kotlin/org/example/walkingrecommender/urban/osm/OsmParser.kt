package org.example.walkingrecommender.urban.osm

import de.topobyte.osm4j.core.model.iface.EntityType
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream

@Component
class OsmParser {

    fun parse(file: File): OsmData {
        val nodes = mutableMapOf<Long, OsmNodeDto>()
        val ways = mutableMapOf<Long, OsmWayDto>()
        val relations = mutableListOf<OsmRelationDto>()

        FileInputStream(file).use { inputStream ->
            val iterator = OsmXmlIterator(inputStream, false)

            for (container in iterator) {
                when (container.type) {
                    EntityType.Node -> {
                        val node = container.entity as OsmNode

                        val tags = buildMap {
                            for (i in 0..<node.numberOfTags) {
                                val tag = node.getTag(i)
                                put(tag.key, tag.value)
                            }
                        }

                        nodes[node.id] = OsmNodeDto(
                            id = node.id,
                            lat = node.latitude,
                            lon = node.longitude,
                            tags = tags,
                        )
                    }

                    EntityType.Way -> {
                        val way = container.entity as OsmWay

                        val nodeIds = buildList {
                            for (i in 0..<way.numberOfNodes) {
                                add(way.getNodeId(i))
                            }
                        }

                        val tags = buildMap {
                            for (i in 0..<way.numberOfTags) {
                                val tag = way.getTag(i)
                                put(tag.key, tag.value)
                            }
                        }

                        ways[way.id] = OsmWayDto(
                            id = way.id,
                            nodeIds = nodeIds,
                            tags = tags,
                        )
                    }

                    EntityType.Relation -> {
                        val relation = container.entity as OsmRelation

                        val tags = buildMap {
                            for (i in 0..<relation.numberOfTags) {
                                val tag = relation.getTag(i)
                                put(tag.key, tag.value)
                            }
                        }

                        if (tags["type"] != "multipolygon") { // only multipolygons
                            continue
                        }

                        val members = buildList {
                            for (i in 0..<relation.numberOfMembers) {
                                val member = relation.getMember(i)
                                if (member.type != EntityType.Way) {
                                    continue
                                }
                                add(
                                    OsmMember(
                                        wayId = member.id,
                                        inner = member.role == "inner",
                                    )
                                )
                            }
                        }

                        relations += OsmRelationDto(
                            id = relation.id,
                            members = members,
                            tags = tags,
                        )
                    }
                }
            }
        }

        return OsmData(
            nodesMap = nodes,
            waysMap = ways,
            nodes = nodes.values.toList(),
            ways = ways.values.toList(),
            relations = relations,
        )
    }
}