package org.simple.clinic.setup

sealed class SetupActivityEvent

data class UserDetailsFetched(val hasUserCompletedOnboarding: Boolean) : SetupActivityEvent()

object DatabaseInitialized: SetupActivityEvent()
