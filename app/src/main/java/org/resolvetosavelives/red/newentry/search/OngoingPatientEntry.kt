package org.resolvetosavelives.red.newentry.search

data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val mobileNumber: String? = null
) {

  data class PersonalDetails(val fullName: String)
}
