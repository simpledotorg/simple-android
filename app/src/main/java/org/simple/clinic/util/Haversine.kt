package org.simple.clinic.util

import org.simple.clinic.location.Coordinates

/**
 * Haversine formula determines the great-circle distance between two points
 * on a sphere given their longitudes and latitudes.
 *
 * Source: https://github.com/jasonwinn/haversine
 * Modified to use with Kotlin.
 */
object Haversine {

  // Earth is not a perfect sphere so this is an approximate value.
  private const val EARTH_RADIUS = 6371.0088

  fun distance(start: Coordinates, end: Coordinates): Distance {
    val latitudeDelta = Math.toRadians(end.latitude - start.latitude)
    val longitudeDelta = Math.toRadians(end.longitude - start.longitude)

    val startLatitudeRadians = Math.toRadians(start.latitude)
    val endLatitudeRadians = Math.toRadians(end.latitude)

    val a = haversine(latitudeDelta) + Math.cos(startLatitudeRadians) * Math.cos(endLatitudeRadians) * haversine(longitudeDelta)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return Distance.ofKilometers(EARTH_RADIUS * c)
  }

  private fun haversine(value: Double): Double {
    return Math.pow(Math.sin(value / 2), 2.0)
  }
}
