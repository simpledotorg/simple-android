package org.simple.clinic.main

import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent
import java.time.Instant

sealed class TheActivityEvent : UiEvent

data class InitialScreenInfoLoaded(
    val user: User,
    val currentTimestamp: Instant,
    val lockAtTimestamp: Optional<Instant>
) : TheActivityEvent()

object UserWasJustVerified : TheActivityEvent()

object UserWasUnauthorized : TheActivityEvent()

object UserWasDisapproved : TheActivityEvent()

object PatientDataCleared: TheActivityEvent()
