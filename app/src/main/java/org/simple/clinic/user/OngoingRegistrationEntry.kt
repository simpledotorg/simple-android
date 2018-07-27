package org.simple.clinic.user

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

data class OngoingRegistrationEntry(
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val pin: String? = null,
    val facilityId: UUID? = null,
    val pinConfirmation: String? = null,
    val createdAt: Instant? = null
)
