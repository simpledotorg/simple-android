package org.simple.clinic.registration.location

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationLocationPermissionEffect

sealed class RegistrationLocationPermissionViewEffect : RegistrationLocationPermissionEffect()

data class OpenFacilitySelectionScreen(val entry: OngoingRegistrationEntry) : RegistrationLocationPermissionViewEffect()
