package org.simple.clinic.location

import androidx.room.ColumnInfo
import org.simple.clinic.util.Distance
import java.lang.Math.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinates(

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double
) {

  companion object {

    // Earth is not a perfect sphere so this is an approximate value.
    private const val EARTH_RADIUS = 6371.0088

    /**
     * Haversine formula determines the great-circle distance between two points
     * on a sphere given their longitudes and latitudes.
     *
     * Source: https://github.com/jasonwinn/haversine
     * Modified to use with Kotlin.
     */
    fun haversineDistance(start: Coordinates, end: Coordinates): Distance {
      val latitudeDelta = toRadians(end.latitude - start.latitude)
      val longitudeDelta = toRadians(end.longitude - start.longitude)

      val startLatitudeRadians = toRadians(start.latitude)
      val endLatitudeRadians = toRadians(end.latitude)

      val a = hav(latitudeDelta) + cos(startLatitudeRadians) * cos(endLatitudeRadians) * hav(longitudeDelta)
      val c = 2 * atan2(Math.sqrt(a), sqrt(1 - a))
      return Distance.ofKilometers(EARTH_RADIUS * c)
    }

    private fun hav(value: Double): Double {
      return sin(value / 2).pow(2)
    }
  }
}
