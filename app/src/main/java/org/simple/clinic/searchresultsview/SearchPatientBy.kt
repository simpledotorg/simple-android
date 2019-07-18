package org.simple.clinic.searchresultsview

sealed class SearchPatientBy {

  data class Name(val searchText: String) : SearchPatientBy()

  data class PhoneNumber(val searchText: String) : SearchPatientBy()
}
