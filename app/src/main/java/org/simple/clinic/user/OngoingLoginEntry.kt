package org.simple.clinic.user

import java.util.UUID

data class OngoingLoginEntry(
    val userId: UUID,
    val phoneNumber: String = "",
    val pin: String = ""
)
