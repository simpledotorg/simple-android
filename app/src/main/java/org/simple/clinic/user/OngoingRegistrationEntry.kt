package org.simple.clinic.user

import org.threeten.bp.Instant
import java.util.UUID

data class OngoingRegistrationEntry(
    val uuid: UUID? = null,
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val pin: String? = null,
    val pinConfirmation: String? = null,
    val facilityIds: List<UUID>? = null,
    val createdAt: Instant? = null
)
