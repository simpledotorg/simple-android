package org.simple.clinic.setup

import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.setup.runcheck.Disallowed

sealed class SetupActivityEffect

data object FetchUserDetails : SetupActivityEffect()

data object GoToMainActivity : SetupActivityEffect()

data object ShowOnboardingScreen : SetupActivityEffect()

data object InitializeDatabase : SetupActivityEffect()

data object ShowCountrySelectionScreen : SetupActivityEffect()

data object RunDatabaseMaintenance : SetupActivityEffect()

data object FetchDatabaseMaintenanceLastRunAtTime : SetupActivityEffect()

data class ShowNotAllowedToRunMessage(val reason: Disallowed.Reason) : SetupActivityEffect()

data object CheckIfAppCanRun : SetupActivityEffect()

data class SaveCountryAndDeployment(
    val country: Country,
    val deployment: Deployment
): SetupActivityEffect()

data object DeleteStoredCountryV1 : SetupActivityEffect()

data object ExecuteDatabaseEncryption : SetupActivityEffect()
