package org.example.walkingrecommender.urban.osm.extractor

import org.example.walkingrecommender.urban.UrbanObject
import org.example.walkingrecommender.urban.UrbanObjectType
import org.example.walkingrecommender.urban.osm.OsmData
import org.example.walkingrecommender.urban.osm.filter.IndustrialOsmFilter
import org.springframework.stereotype.Component

@Component
class IndustrialExtractor(
    geometryBuilder: GeometryBuilder,
    private val industrialOsmFilter: IndustrialOsmFilter,
) : UrbanObjectExtractor(geometryBuilder) {

    override fun filterData(data: OsmData): OsmData {
        return industrialOsmFilter.filterIndustrial(data)
    }

    override fun filterUrbanObject(obj: UrbanObject): Boolean {
        return true
    }

    override fun getUrbanObjectType(): UrbanObjectType = UrbanObjectType.INDUSTRIAL_AREA

    override fun isClosedWay(): Boolean = true

    override fun getName(tags: Map<String, String>): String? {
        return tags["name"]
    }
}