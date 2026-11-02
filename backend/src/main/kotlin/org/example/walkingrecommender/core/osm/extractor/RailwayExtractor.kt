package org.example.walkingrecommender.core.osm.extractor

import org.example.walkingrecommender.core.model.UrbanObject
import org.example.walkingrecommender.core.model.UrbanObjectType
import org.example.walkingrecommender.core.osm.OsmData
import org.example.walkingrecommender.core.osm.filter.RailwayOsmFilter
import org.springframework.stereotype.Component

@Component
class RailwayExtractor(
    geometryBuilder: GeometryBuilder,
    private val railwayOsmFilter: RailwayOsmFilter,
) : UrbanObjectExtractor(geometryBuilder) {

    override fun filterData(data: OsmData): OsmData {
        return railwayOsmFilter.filterRailway(data)
    }

    override fun filterUrbanObject(obj: UrbanObject): Boolean {
        return true
    }

    override fun getUrbanObjectType(): UrbanObjectType = UrbanObjectType.RAILWAY

    override fun isClosedWay(): Boolean = true

    override fun getName(tags: Map<String, String>): String? {
        return tags["name"]
    }
}