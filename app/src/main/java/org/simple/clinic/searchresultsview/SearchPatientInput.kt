package org.simple.clinic.searchresultsview

sealed class SearchPatientInput {

  data class Name(val searchText: String) : SearchPatientInput()

  data class PhoneNumber(val searchText: String) : SearchPatientInput()
}
