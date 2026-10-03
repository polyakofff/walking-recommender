package org.example.walkingrecommender.core.osm.extractor

import org.example.walkingrecommender.core.model.OsmElementType
import org.example.walkingrecommender.core.model.UrbanObject
import org.example.walkingrecommender.core.model.UrbanObjectType
import org.example.walkingrecommender.core.osm.OsmData

abstract class UrbanObjectExtractor(
    private val geometryBuilder: GeometryBuilder,
) {

    fun extract(data: OsmData): List<UrbanObject> {
        val filtered = filterData(data)

        val objects = filtered.nodes.map {
            UrbanObject(
                id = -1,
                osmElementType = OsmElementType.NODE,
                osmId = it.id,
                urbanObjectType = getUrbanObjectType(),
                geometry = geometryBuilder.buildPoint(it),
                name = getName(it.tags),
                tags = it.tags,
            )
        } + filtered.ways.mapNotNull {
            UrbanObject(
                id = -1,
                osmElementType = OsmElementType.WAY,
                osmId = it.id,
                urbanObjectType = getUrbanObjectType(),
                geometry = if (isClosedWay()) {
                    try {
                        geometryBuilder.buildPolygon(it, data)
                    } catch (ex: Exception) {
                        println("osm way id: ${it.id}, error: ${ex.message}")
                        return@mapNotNull null
                    }
                } else {
                    geometryBuilder.buildLineString(it, data)
                },
                name = getName(it.tags),
                tags = it.tags,
            )
        } + filtered.relations.map {
            UrbanObject(
                id = -1,
                osmElementType = OsmElementType.RELATION,
                osmId = it.id,
                urbanObjectType = getUrbanObjectType(),
                geometry = geometryBuilder.buildMultipolygon(it, data),
                name = getName(it.tags),
                tags = it.tags,
            )
        }

        return objects.filter { filterUrbanObject(it) }
    }

    protected abstract fun filterData(data: OsmData): OsmData

    protected abstract fun filterUrbanObject(obj: UrbanObject): Boolean

    abstract fun getUrbanObjectType(): UrbanObjectType

    protected abstract fun isClosedWay(): Boolean

    protected abstract fun getName(tags: Map<String, String>): String?
}