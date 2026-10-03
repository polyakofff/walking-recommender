package org.example.walkingrecommender.core

import org.example.walkingrecommender.core.model.Node
import org.example.walkingrecommender.core.osm.OsmNodeDto
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Point
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

val averageWalkingSpeedMpS = 1.67

fun distance(n1: OsmNodeDto, n2: OsmNodeDto): Double {
    return distance(Coordinate(n1.lon, n1.lat), Coordinate(n2.lon, n2.lat))
}

fun distance(n1: Node, n2: Node): Double {
    return distance(n1.geometry.coordinate, n2.geometry.coordinate)
}

fun distance(p1: Point, p2: Point): Double {
    return distance(p1.coordinate, p2.coordinate)
}

/**
 * Calculate distance between 2 geographical points (in meters)
 * Haversine formula (https://en.wikipedia.org/wiki/Haversine_formula)
 */
@Deprecated("Use distance instead")
fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0

    val lat1Rad = Math.toRadians(lat1)
    val lon1Rad = Math.toRadians(lon1)
    val lat2Rad = Math.toRadians(lat2)
    val lon2Rad = Math.toRadians(lon2)

    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    val a = sin(dLat / 2).pow(2.0) +
            cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2.0)

    return r * 2 * asin(sqrt(a))
}

/**
 * Calculate distance between 2 geographical points (in meters)
 * Haversine formula (https://en.wikipedia.org/wiki/Haversine_formula)
 */
fun distance(c1: Coordinate, c2: Coordinate): Double {
    val r = 6371000.0

    val lat1 = Math.toRadians(c1.y)
    val lon1 = Math.toRadians(c1.x)
    val lat2 = Math.toRadians(c2.y)
    val lon2 = Math.toRadians(c2.x)

    val dLat = lat2 - lat1
    val dLon = lon2 - lon1

    val a = sin(dLat / 2).pow(2.0) +
            cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)

    return r * 2 * asin(sqrt(a))
}

/**
 * Calculate distance (meters) by time (seconds) and speed (meters/second)
 */
fun distance(time: Int, speed: Double = averageWalkingSpeedMpS): Double {
    return time * speed
}

/**
 * Calculate time (seconds) by distance (meters) and speed (meters/seconds)
 */
fun time(distance: Double, speed: Double = averageWalkingSpeedMpS): Double {
    return distance / speed
}

/**
 * Angle between North - c1 - c2 in 0..360 degrees
 */
fun northAngle(c1: Coordinate, c2: Coordinate): Double {
    val lat1 = Math.toRadians(c1.y)
    val lon1 = Math.toRadians(c1.x)
    val lat2 = Math.toRadians(c2.y)
    val lon2 = Math.toRadians(c2.x)

    val dLon = lon2 - lon1

    val east = cos(lat2) * sin(dLon)
    val north = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

    return (Math.toDegrees(atan2(east, north)) + 360.0) % 360.0
}