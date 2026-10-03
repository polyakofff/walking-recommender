package org.example.walkingrecommender.core.feature

import org.example.walkingrecommender.core.db.NearbyUrbanObject
import org.example.walkingrecommender.core.model.Edge
import org.example.walkingrecommender.core.model.EdgeSegment
import org.example.walkingrecommender.core.model.UrbanObjectType
import org.springframework.stereotype.Component
import kotlin.math.exp
import kotlin.math.max

data class Features(
    val greenery: Double = 0.0,
    val water: Double = 0.0,

    val traffic: Double = 0.0,
    val noise: Double = 0.0,

    val historicArchitecture: Double = 0.0,
    val modernArchitecture: Double = 0.0,

    val commercialActivity: Double = 0.0,
    val grittiness: Double = 0.0,

    val pedestrianComfort: Double = 0.0,
)

@Component
class FeatureCalculator {

    fun calculateSegment(
        edge: Edge,
        segment: EdgeSegment,
        withinObjects: List<NearbyUrbanObject>,
    ): Features {

        var segmentTraffic = 0.0
        var segmentNoise = 0.0
        var segmentPedestrianComfort = 0.0

        when (edge.highway) {
            "footway" -> {
                segmentTraffic = 0.0
                segmentNoise = 0.0
                segmentPedestrianComfort = 0.5
            }
            "path" -> {
                segmentTraffic = 0.0
                segmentNoise = 0.0
                segmentPedestrianComfort = 0.4
            }
            "steps" -> {
                segmentTraffic = 0.0
                segmentNoise = 0.0
                segmentPedestrianComfort = 0.1
            }
            "pedestrian" -> {
                segmentTraffic = 0.0
                segmentNoise = 0.0
                segmentPedestrianComfort = 1.0
            }
            "track" -> {
                segmentTraffic = 0.0
                segmentNoise = 0.0
                segmentPedestrianComfort = 0.4
            }
            "cycleway" -> {
                segmentTraffic = 0.0
                segmentNoise = 0.0
                segmentPedestrianComfort = 0.4
            }
            "residential" -> {
                segmentTraffic = 0.5
                segmentNoise = 0.2
                segmentPedestrianComfort = 0.3
            }
            "living_street" -> {
                segmentTraffic = 0.4
                segmentNoise = 0.1
                segmentPedestrianComfort = 0.4
            }
            "primary", "primary_link" -> {
                segmentTraffic = 0.9
                segmentNoise = 0.9
                segmentPedestrianComfort = 0.0
                // TODO: lanes, maxspeed
            }
            "secondary", "secondary_link" -> {
                segmentTraffic = 0.8
                segmentNoise = 0.8
                segmentPedestrianComfort = 0.1
                // TODO: lanes, maxspeed
            }
            "tertiary", "tertiary_link" -> {
                segmentTraffic = 0.7
                segmentNoise = 0.7
                segmentPedestrianComfort = 0.2
                // TODO: lanes, maxspeed
            }
            else -> {}
        }

        // TODO: edge surface, lit

        val withinObjectsMap = withinObjects.groupBy { it.urbanObjectType }

        val parkFeatures = parkInfluence(withinObjectsMap[UrbanObjectType.PARK].orEmpty())
        val forestFeatures = forestInfluence(withinObjectsMap[UrbanObjectType.FOREST].orEmpty())
        val waterFeatures = waterInfluence(withinObjectsMap[UrbanObjectType.WATER].orEmpty())
        val industrialFeatures = industrialInfluence(withinObjectsMap[UrbanObjectType.INDUSTRIAL_AREA].orEmpty())
        val garagesFeatures = garagesInfluence(withinObjectsMap[UrbanObjectType.GARAGES].orEmpty())
        val railwayFeatures = railwayInfluence(withinObjectsMap[UrbanObjectType.RAILWAY].orEmpty())
        val buildingFeatures = buildingInfluence(withinObjectsMap[UrbanObjectType.BUILDING].orEmpty())
        val roadFeatures = roadInfluence(withinObjectsMap[UrbanObjectType.ROAD].orEmpty(), edge.wayOsmId)

        val inPark = withinObjectsMap[UrbanObjectType.PARK].orEmpty().any { obj -> obj.distance == 0.0 }
        val inForest = withinObjectsMap[UrbanObjectType.FOREST].orEmpty().any { obj -> obj.distance == 0.0 }

        return Features(
            greenery = when {
                inForest -> 1.0
                inPark -> 0.9
                else -> combine(
                    parkFeatures.greenery,
                    forestFeatures.greenery,
                )
            },
            water = waterFeatures.water,
            traffic = combine(
                segmentTraffic,
                roadFeatures.traffic,
            ),
            noise = combine(
                segmentNoise,
                railwayFeatures.noise,
                roadFeatures.noise,
            ),
            historicArchitecture = buildingFeatures.historicArchitecture,
            modernArchitecture = buildingFeatures.modernArchitecture,
            commercialActivity = buildingFeatures.commercialActivity,
            grittiness = combine(
                industrialFeatures.grittiness,
                garagesFeatures.grittiness,
                railwayFeatures.grittiness,
            ),
            pedestrianComfort = segmentPedestrianComfort,
        )
    }

    private fun parkInfluence(objects: List<NearbyUrbanObject>): Features {
        var greenery = 0.0
        objects.forEach { obj ->
            val factor = distanceFactor(UrbanObjectType.PARK, obj.distance)
            greenery = combine(greenery, 0.4 * factor)
        }
        return Features(
            greenery = greenery,
        )
    }

    private fun forestInfluence(objects: List<NearbyUrbanObject>): Features {
        var greenery = 0.0
        objects.forEach { obj ->
            val factor = distanceFactor(UrbanObjectType.FOREST, obj.distance)
            greenery = combine(greenery, 0.5 * factor)
        }
        return Features(
            greenery = greenery,
        )
    }

    private fun waterInfluence(objects: List<NearbyUrbanObject>): Features {
        var water = 0.0
        objects.forEach { obj ->
            val factor = distanceFactor(UrbanObjectType.WATER, obj.distance)
            water = combine(water, 0.5 * factor)
        }
        return Features(
            water = water,
        )
    }

    private fun industrialInfluence(objects: List<NearbyUrbanObject>): Features {
        var grittiness = 0.0
        objects.forEach { obj ->
            val factor = distanceFactor(UrbanObjectType.INDUSTRIAL_AREA, obj.distance)
            grittiness = combine(grittiness, 1.0 * factor)
        }
        return Features(
            grittiness = grittiness,
        )
    }

    private fun garagesInfluence(objects: List<NearbyUrbanObject>): Features {
        var grittiness = 0.0
        objects.forEach { obj ->
            val factor = distanceFactor(UrbanObjectType.GARAGES, obj.distance)
            grittiness = combine(grittiness, 0.4 * factor)
        }
        return Features(
            grittiness = grittiness,
        )
    }

    private fun railwayInfluence(objects: List<NearbyUrbanObject>): Features {
        var noise = 0.0
        var grittiness = 0.0
        objects.forEach { obj ->
            val factor = distanceFactor(UrbanObjectType.RAILWAY, obj.distance)
            noise = combine(noise, 0.5 * factor)
            grittiness = combine(grittiness, 0.7 * factor)
        }
        return Features(
            noise = noise,
            grittiness = grittiness,
        )
    }

    private fun buildingInfluence(objects: List<NearbyUrbanObject>): Features {
        var historicArchitecture = 0.0
        var modernArchitecture = 0.0
        var commercialActivity = 0.0
        objects.forEach { obj ->
            val factor = distanceFactor(UrbanObjectType.BUILDING, obj.distance)
            historicArchitecture = combine(historicArchitecture, historicity(obj.tags) * factor)
            modernArchitecture = combine(modernArchitecture, modernity(obj.tags) * factor)
            commercialActivity = combine(commercialActivity, commerciality(obj.tags) * factor)
        }
        return Features(
            historicArchitecture = historicArchitecture,
            modernArchitecture = modernArchitecture,
            commercialActivity = commercialActivity,
        )
    }

    private fun roadInfluence(objects: List<NearbyUrbanObject>, segmentOsmId: Long): Features {
        var traffic = 0.0
        var noise = 0.0
        objects.forEach { obj ->
            if (obj.osmId == segmentOsmId) {
                // дорога == текущий сегмент
                return@forEach
            }
            val coef = roadCoef(obj.tags)
            val factor = distanceFactor(UrbanObjectType.ROAD, obj.distance)
            traffic = combine(traffic, coef * factor)
            noise = combine(noise, coef * factor)
        }
        return Features(
            traffic = traffic,
            noise = noise,
        )
    }

    private fun combine(vararg values: Double): Double =
        1.0 - values.fold(1.0) { acc, value ->
            acc * (1.0 - value.coerceIn(0.0, 1.0))
        }

    private fun distanceInfluenceExp(distanceMeters: Double, decayMeters: Double): Double =
        exp(-distanceMeters / decayMeters)

    private fun distanceFactor(type: UrbanObjectType, distance: Double): Double {
        val radius = influenceRadiusMeters(type)
        return max(0.0, (radius - distance) / radius)
    }

    private fun influenceRadiusMeters(type: UrbanObjectType): Int =
        when (type) {
            UrbanObjectType.PARK -> 100
            UrbanObjectType.FOREST -> 300
            UrbanObjectType.WATER -> 300
            UrbanObjectType.INDUSTRIAL_AREA -> 300
            UrbanObjectType.GARAGES -> 50
            UrbanObjectType.RAILWAY -> 300
            UrbanObjectType.BUILDING -> 50
            UrbanObjectType.ROAD -> 100
        }

    private fun historicity(tags: Map<String, String>): Double {
        if ("historic" in tags) {
            return 1.0
        }

        val year = tags["start_date"]
            ?.take(4)
            ?.toIntOrNull()
            ?: return 0.0

        return when {
            year <= 1917 -> 1.0
            year <= 1940 -> 0.7
            year <= 1955 -> 0.5
            else -> 0.0
        }
    }

    private fun modernity(tags: Map<String, String>): Double {
        val year = tags["start_date"]
            ?.take(4)
            ?.toIntOrNull()
            ?: -1

        return when {
            year >= 2020 -> 1.0
            year >= 2010 -> 0.7
            year >= 2000 -> 0.5
            else -> 0.0
        }
    }

    private fun commerciality(tags: Map<String, String>): Double {
        return when {
            tags["building"] == "commercial" -> 1.0
            tags["building"] == "retail" -> 0.7
            else -> 0.0
        }
    }

    private fun roadCoef(tags: Map<String, String>): Double {
        val lanes = tags["lanes"] ?: 2
        val maxspeed = tags["maxspeed"] ?: 60

        return when (tags["highway"]) {
            "motorway",
            "trunk",
            "primary",
            "primary_link" -> 0.9
            "secondary",
            "secondary_link" -> 0.8
            "tertiary",
            "tertiary_link" -> 0.7
            "residential",
            "living_street" -> 0.5
            "unclassified" -> 0.5
            else -> 0.0
        }
    }
}