package org.simple.clinic.facility.alertchange

sealed interface AlertFacilityChangeEffect {
  data object LoadIsFacilityChangedStatus : AlertFacilityChangeEffect
}
