package org.simple.clinic.registration.location

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationLocationPermissionUiActions {
  fun openFacilitySelectionScreen(registrationEntry: OngoingRegistrationEntry)
}
