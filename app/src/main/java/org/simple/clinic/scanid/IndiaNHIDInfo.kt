package org.simple.clinic.scanid

data class IndiaNHIDInfo(
    val healthIdNumber: String,
    val healthIdUserName: String,
    val fullName: String,
    val gender: String,
    val state: String?,
    val district: String?,
    val dateOfBirth: String,
    val address: String
)
