package org.simple.clinic.util

data class Distance(val millimeters: Long) : Comparable<Distance> {

  override operator fun compareTo(other: Distance): Int {
    return millimeters.compareTo(other.millimeters)
  }

  companion object {

    fun ofKilometers(km: Double): Distance {
      return ofMeters(km * 1000.0)
    }

    fun ofMeters(m: Double): Distance {
      return Distance(millimeters = (m * 1000).toLong())
    }
  }
}
