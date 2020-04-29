package org.simple.clinic.user

import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import java.util.UUID

data class OngoingRegistrationEntry(
    val uuid: UUID? = null,
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val pin: String? = null,
    val pinConfirmation: String? = null,
    val facilityId: UUID? = null,
    val createdAt: Instant? = null
) {

  fun withPinConfirmation(pinConfirmation: String, clock: UtcClock): OngoingRegistrationEntry {
    check(this.pin == pinConfirmation) { "Stored PIN != Entered PIN confirmation!" }

    return this.copy(pinConfirmation = pinConfirmation, createdAt = Instant.now(clock))
  }

  fun resetPin(): OngoingRegistrationEntry {
    return this.copy(pin = null, pinConfirmation = null)
  }
}
