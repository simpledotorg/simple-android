package org.simple.clinic.summary.teleconsultation.api

sealed class TeleconsultInfo {
  object Fetching : TeleconsultInfo()

  data class Fetched(val doctorPhoneNumber: String) : TeleconsultInfo()

  object NetworkError : TeleconsultInfo()

  object MissingPhoneNumber : TeleconsultInfo()
}
