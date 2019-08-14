package org.simple.clinic.facility

sealed class FacilityPullResult {

  object Success : FacilityPullResult()

  object NetworkError : FacilityPullResult()

  object UnexpectedError : FacilityPullResult()
}
