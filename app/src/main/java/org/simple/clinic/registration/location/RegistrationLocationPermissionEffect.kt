package org.simple.clinic.registration.location

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationLocationPermissionEffect

data class OpenFacilitySelectionScreen(val entry: OngoingRegistrationEntry) : RegistrationLocationPermissionEffect()
