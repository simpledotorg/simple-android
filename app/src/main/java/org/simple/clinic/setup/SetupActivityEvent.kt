package org.simple.clinic.setup

import org.simple.clinic.appconfig.Country
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import java.time.Instant

sealed class SetupActivityEvent

data class UserDetailsFetched(
    val hasUserCompletedOnboarding: Boolean,
    val loggedInUser: Optional<User>,
    val userSelectedCountry: Optional<Country>
) : SetupActivityEvent()

object DatabaseInitialized : SetupActivityEvent()

object FallbackCountrySetAsSelected : SetupActivityEvent()

object DatabaseMaintenanceCompleted : SetupActivityEvent()

data class DatabaseMaintenanceLastRunAtTimeLoaded(val runAt: Optional<Instant>): SetupActivityEvent()
