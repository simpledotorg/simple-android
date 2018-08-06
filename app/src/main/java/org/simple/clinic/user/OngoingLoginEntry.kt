package org.simple.clinic.user

data class OngoingLoginEntry(
    val otp: String? = null,
    val phoneNumber: String? = null,
    val pin: String? = null
)
