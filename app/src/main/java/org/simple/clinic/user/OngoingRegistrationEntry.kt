package org.simple.clinic.user

import org.threeten.bp.LocalDate

data class OngoingRegistrationEntry(
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val pin: String? = null,
    val pinConfirmation: String? = null,
    val createdAt: LocalDate? = null
)
