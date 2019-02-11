package org.simple.clinic.location

sealed class LocationUpdate {
  object Unavailable : LocationUpdate()
  data class Available(val location: Coordinates) : LocationUpdate()
}
