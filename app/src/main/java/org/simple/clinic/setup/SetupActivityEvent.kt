package org.simple.clinic.setup

import org.simple.clinic.user.User
import org.simple.clinic.util.Optional

sealed class SetupActivityEvent

data class UserDetailsFetched(
    val hasUserCompletedOnboarding: Boolean,
    val loggedInUser: Optional<User>
) : SetupActivityEvent()

object DatabaseInitialized : SetupActivityEvent()
