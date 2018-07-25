package org.simple.clinic.user

data class OngoingRegistrationEntry(
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val pin: String? = null,
    val pinConfirmation: String? = null
)
