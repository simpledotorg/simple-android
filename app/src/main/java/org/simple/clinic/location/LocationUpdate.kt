package org.simple.clinic.location

sealed class LocationUpdate {
  object TurnedOff : LocationUpdate()
  data class Available(val location: Coordinates) : LocationUpdate()
}
