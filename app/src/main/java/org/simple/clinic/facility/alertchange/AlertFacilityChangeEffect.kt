package org.simple.clinic.facility.alertchange

sealed interface AlertFacilityChangeEffect {
  data object LoadIsFacilityChangedStatus : AlertFacilityChangeEffect

  data object MarkFacilityChangedAsFalse : AlertFacilityChangeEffect
}

sealed interface AlertFacilityChangeViewEffect : AlertFacilityChangeEffect {
  data object CloseSheetWithContinuation : AlertFacilityChangeViewEffect
}
