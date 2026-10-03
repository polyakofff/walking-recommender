package org.example.walkingrecommender.core.osm.extractor

import org.example.walkingrecommender.core.model.UrbanObject
import org.example.walkingrecommender.core.model.UrbanObjectType
import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.filter.WaterOsmFilter
import org.springframework.stereotype.Component

@Component
class WaterExtractor(
    geometryBuilder: GeometryBuilder,
    private val waterOsmFilter: WaterOsmFilter,
) : UrbanObjectExtractor(geometryBuilder) {

    val minimalAreaSquareMeters = 100.0

    override fun filterData(data: OsmData): OsmData {
        return waterOsmFilter.filterWater(data)
    }

    override fun filterUrbanObject(obj: UrbanObject): Boolean {
//        return obj.geometry.area >= minimalAreaSquareMeters
        return true
    }

    override fun getUrbanObjectType(): UrbanObjectType = UrbanObjectType.WATER

    override fun isClosedWay(): Boolean = true

    override fun getName(tags: Map<String, String>): String? {
        return tags["name"]
    }
}