package org.example.walkingrecommender.urban

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.springframework.stereotype.Component

data class LineSegment(
    val lineString: LineString,
    val lengthMeters: Double,
)

@Component
class GeometryUtils(private val geometryFactory: GeometryFactory) {

    fun splitLineStringByMeters(lineString: LineString, maxMeters: Double): List<LineSegment> {
        val result = mutableListOf<LineSegment>()

        val coordinates = lineString.coordinates
        var current = mutableListOf(coordinates[0])
        var currentLength = 0.0

        for (i in 1..<coordinates.size) {
            var from = coordinates[i - 1]
            val to = coordinates[i]

            var remainingSegmentLength = distance(from, to)

            while (currentLength + remainingSegmentLength > maxMeters) {
                val ratio = (maxMeters - currentLength) / remainingSegmentLength

                val splitPoint = Coordinate(
                    from.x + (to.x - from.x) * ratio,
                    from.y + (to.y - from.y) * ratio
                )

                current += splitPoint
                result += LineSegment(
                    lineString = geometryFactory.createLineString(current.toTypedArray()),
                    lengthMeters = maxMeters,
                )

                current = mutableListOf(splitPoint)
                currentLength = 0.0
                from = splitPoint
                remainingSegmentLength = distance(from, to)
            }

            current += to
            currentLength += remainingSegmentLength
        }

        if (current.size >= 2) {
            result += LineSegment(
                lineString = geometryFactory.createLineString(current.toTypedArray()),
                lengthMeters = currentLength,
            )
        }

        return result
    }
}