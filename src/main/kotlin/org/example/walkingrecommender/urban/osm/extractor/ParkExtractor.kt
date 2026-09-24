package org.example.walkingrecommender.urban.osm.extractor

import org.example.walkingrecommender.urban.UrbanObject
import org.example.walkingrecommender.urban.UrbanObjectType
import org.example.walkingrecommender.urban.osm.OsmData
import org.example.walkingrecommender.urban.osm.filter.ParkOsmFilter
import org.springframework.stereotype.Component

@Component
class ParkExtractor(
    geometryBuilder: GeometryBuilder,
    private val parkOsmFilter: ParkOsmFilter,
) : UrbanObjectExtractor(geometryBuilder) {

    override fun filterData(data: OsmData): OsmData {
        return parkOsmFilter.filterParks(data)
    }

    override fun filterUrbanObject(obj: UrbanObject): Boolean {
        return true
    }

    override fun getUrbanObjectType(): UrbanObjectType = UrbanObjectType.PARK

    override fun isClosedWay(): Boolean = true

    override fun getName(tags: Map<String, String>): String? {
        return tags["name"]
    }
}