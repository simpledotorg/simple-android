package org.simple.clinic.main

import org.simple.clinic.navigation.v2.History
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.util.Optional

sealed class TheActivityEvent : UiEvent

data class InitialScreenInfoLoaded(
    val user: User,
    val currentTimestamp: Instant,
    val lockAtTimestamp: Optional<Instant>,
    val currentHistory: History
) : TheActivityEvent()

data object UserWasJustVerified : TheActivityEvent()

data object UserWasUnauthorized : TheActivityEvent()

data object UserWasDisapproved : TheActivityEvent()

data object PatientDataCleared : TheActivityEvent()
