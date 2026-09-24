package org.example.walkingrecommender.urban.osm.extractor

import org.example.walkingrecommender.urban.UrbanObject
import org.example.walkingrecommender.urban.UrbanObjectType
import org.example.walkingrecommender.urban.osm.OsmData
import org.example.walkingrecommender.urban.osm.filter.RoadOsmFilter
import org.springframework.stereotype.Component

@Component
class RoadExtractor(
    geometryBuilder: GeometryBuilder,
    private val roadOsmFilter: RoadOsmFilter,
) : UrbanObjectExtractor(geometryBuilder) {

    override fun filterData(data: OsmData): OsmData {
        return roadOsmFilter.filterRoad(data)
    }

    override fun filterUrbanObject(obj: UrbanObject): Boolean {
        return true
    }

    override fun getUrbanObjectType(): UrbanObjectType = UrbanObjectType.ROAD

    override fun isClosedWay(): Boolean = false

    override fun getName(tags: Map<String, String>): String? {
        return tags["name"]
    }
}