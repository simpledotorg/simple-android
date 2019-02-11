package org.simple.clinic.util

sealed class Distance {
  abstract fun meters(): Double
  abstract fun kilometers(): Double

  operator fun compareTo(other: Distance): Int {
    return meters().compareTo(other.meters())
  }
}

data class Kilometers(val km: Double): Distance() {
  override fun meters() = km * 1000
  override fun kilometers() = km
}
