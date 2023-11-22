package org.simple.clinic.facility.alertchange

sealed interface AlertFacilityChangeEffect {
  data object LoadIsFacilityChangedStatus : AlertFacilityChangeEffect
}

sealed interface AlertFacilityChangeViewEffect : AlertFacilityChangeEffect {
  data object CloseSheetWithContinuation : AlertFacilityChangeViewEffect
}
