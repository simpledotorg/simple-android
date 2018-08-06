package org.simple.clinic.user

data class OngoingLoginEntry(
    val otp: String,
    val phoneNumber: String? = null,
    val pin: String? = null
)
