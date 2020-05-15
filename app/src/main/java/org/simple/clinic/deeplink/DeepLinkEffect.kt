package org.simple.clinic.deeplink

import java.util.UUID

sealed class DeepLinkEffect

object FetchUser : DeepLinkEffect()

object NavigateToSetupActivity : DeepLinkEffect()

object NavigateToMainActivity : DeepLinkEffect()

data class FetchPatient(val patientUuid: UUID) : DeepLinkEffect()

data class NavigateToPatientSummary(val patientUuid: UUID) : DeepLinkEffect()
