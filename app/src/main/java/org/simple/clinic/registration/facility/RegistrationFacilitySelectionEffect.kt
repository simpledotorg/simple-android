package org.simple.clinic.registration.facility

import org.simple.clinic.facility.Facility
import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationFacilitySelectionEffect

data class MoveToIntroVideoScreen(val registrationEntry: OngoingRegistrationEntry) : RegistrationFacilitySelectionEffect()

sealed class RegistrationFacilitySelectionViewEffect : RegistrationFacilitySelectionEffect()

data class OpenConfirmFacilitySheet(val facility: Facility) : RegistrationFacilitySelectionViewEffect()
