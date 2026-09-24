package org.example.walkingrecommender.urban.osm.extractor

import org.example.walkingrecommender.urban.UrbanObject
import org.example.walkingrecommender.urban.UrbanObjectType
import org.example.walkingrecommender.urban.osm.OsmData
import org.example.walkingrecommender.urban.osm.filter.ForestOsmFilter
import org.springframework.stereotype.Component

@Component
class ForestExtractor(
    geometryBuilder: GeometryBuilder,
    private val forestOsmFilter: ForestOsmFilter,
) : UrbanObjectExtractor(geometryBuilder) {

    val minimalAreaSquareMeters = 1000.0

    override fun filterData(data: OsmData): OsmData {
        return forestOsmFilter.filterForests(data)
    }

    override fun filterUrbanObject(obj: UrbanObject): Boolean {
//        return obj.geometry.area >= minimalAreaSquareMeters
        return true
    }

    override fun getUrbanObjectType(): UrbanObjectType = UrbanObjectType.FOREST

    override fun isClosedWay(): Boolean = true

    override fun getName(tags: Map<String, String>): String? {
        return tags["name"]
    }
}