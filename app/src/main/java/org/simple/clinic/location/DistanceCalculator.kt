package org.simple.clinic.location

import org.simple.clinic.util.Distance
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// Earth is not a perfect sphere so this is an approximate value.
private const val EARTH_RADIUS = 6371.0088

class DistanceCalculator @Inject constructor() {

  /**
   * Calculate great-circle distance between two points on a sphere given
   * their longitudes and latitudes using Haversine formula.
   *
   * Source: https://github.com/jasonwinn/haversine
   * Modified to use with Kotlin.
   */
  fun between(start: Coordinates, end: Coordinates): Distance {
    val latitudeDelta = Math.toRadians(end.latitude - start.latitude)
    val longitudeDelta = Math.toRadians(end.longitude - start.longitude)

    val startLatitudeRadians = Math.toRadians(start.latitude)
    val endLatitudeRadians = Math.toRadians(end.latitude)

    val a = hav(latitudeDelta) + cos(startLatitudeRadians) * cos(endLatitudeRadians) * hav(longitudeDelta)
    val c = 2 * atan2(Math.sqrt(a), sqrt(1 - a))
    return Distance.ofKilometers(EARTH_RADIUS * c)
  }

  private fun hav(value: Double): Double {
    return sin(value / 2).pow(2)
  }
}
