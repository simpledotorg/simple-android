package org.simple.clinic.facility

sealed class FacilityPullResult {

  class Success : FacilityPullResult()

  class NetworkError : FacilityPullResult()

  class UnexpectedError : FacilityPullResult()
}
