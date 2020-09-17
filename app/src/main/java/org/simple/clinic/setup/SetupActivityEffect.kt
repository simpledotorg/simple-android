package org.simple.clinic.setup

sealed class SetupActivityEffect

object FetchUserDetails : SetupActivityEffect()

object GoToMainActivity : SetupActivityEffect()

object ShowOnboardingScreen : SetupActivityEffect()

object InitializeDatabase : SetupActivityEffect()

object ShowCountrySelectionScreen : SetupActivityEffect()

object SetFallbackCountryAsCurrentCountry: SetupActivityEffect()

object RunDatabaseMaintenance: SetupActivityEffect()

object FetchDatabaseMaintenanceLastRunAtTime: SetupActivityEffect()
