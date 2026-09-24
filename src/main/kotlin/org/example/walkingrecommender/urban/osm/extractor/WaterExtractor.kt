package org.example.walkingrecommender.urban.osm.extractor

import org.example.walkingrecommender.urban.UrbanObject
import org.example.walkingrecommender.urban.UrbanObjectType
import org.example.walkingrecommender.urban.osm.OsmData
import org.example.walkingrecommender.urban.osm.filter.WaterOsmFilter
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