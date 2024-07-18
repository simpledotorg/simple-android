package org.simple.clinic.facility

sealed class FacilityPullResult {

  data object Success : FacilityPullResult()

  data object NetworkError : FacilityPullResult()

  data object UnexpectedError : FacilityPullResult()
}
