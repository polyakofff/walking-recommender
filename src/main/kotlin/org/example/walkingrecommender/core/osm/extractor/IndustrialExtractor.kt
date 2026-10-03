package org.example.walkingrecommender.core.osm.extractor

import org.example.walkingrecommender.core.model.UrbanObject
import org.example.walkingrecommender.core.model.UrbanObjectType
import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.filter.IndustrialOsmFilter
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