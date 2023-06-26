package org.simple.clinic.setup

import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.setup.runcheck.Disallowed

sealed class SetupActivityEffect

object FetchUserDetails : SetupActivityEffect()

object GoToMainActivity : SetupActivityEffect()

object ShowOnboardingScreen : SetupActivityEffect()

object InitializeDatabase : SetupActivityEffect()

object ShowCountrySelectionScreen : SetupActivityEffect()

object RunDatabaseMaintenance : SetupActivityEffect()

object FetchDatabaseMaintenanceLastRunAtTime : SetupActivityEffect()

data class ShowNotAllowedToRunMessage(val reason: Disallowed.Reason) : SetupActivityEffect()

object CheckIfAppCanRun : SetupActivityEffect()

data class SaveCountryAndDeployment(
    val country: Country,
    val deployment: Deployment
): SetupActivityEffect()

object DeleteStoredCountryV1 : SetupActivityEffect()

object ExecuteDatabaseEncryption : SetupActivityEffect()
