package org.simple.clinic.setup

import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.setup.runcheck.AllowedToRun
import org.simple.clinic.user.User
import java.time.Instant
import java.util.Optional

sealed class SetupActivityEvent

data class UserDetailsFetched(
    val hasUserCompletedOnboarding: Boolean,
    val loggedInUser: Optional<User>,
    val userSelectedCountry: Optional<Country>,
    val userSelectedCountryV1: Optional<Map<String, String>>,
    val currentDeployment: Optional<Deployment>
) : SetupActivityEvent()

data object DatabaseInitialized : SetupActivityEvent()

data object DatabaseMaintenanceCompleted : SetupActivityEvent()

data class DatabaseMaintenanceLastRunAtTimeLoaded(val runAt: Optional<Instant>) : SetupActivityEvent()

data class AppAllowedToRunCheckCompleted(val allowedToRun: AllowedToRun) : SetupActivityEvent()

data object CountryAndDeploymentSaved : SetupActivityEvent()

data object StoredCountryV1Deleted : SetupActivityEvent()

data object DatabaseEncryptionFinished : SetupActivityEvent()

data class MinimumMemoryChecked(val hasMinimumMemory: Boolean) : SetupActivityEvent()
