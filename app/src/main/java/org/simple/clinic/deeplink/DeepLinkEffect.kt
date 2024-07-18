package org.simple.clinic.deeplink

import java.util.UUID

sealed class DeepLinkEffect

data object FetchUser : DeepLinkEffect()

data object NavigateToSetupActivity : DeepLinkEffect()

data class FetchPatient(val patientUuid: UUID) : DeepLinkEffect()

data class NavigateToPatientSummary(val patientUuid: UUID) : DeepLinkEffect()

data class NavigateToPatientSummaryWithTeleconsultLog(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : DeepLinkEffect()

data object ShowPatientDoesNotExist : DeepLinkEffect()

// We will show this error when patient uuid is null
data object ShowNoPatientUuidError : DeepLinkEffect()

data object ShowTeleconsultLogNotAllowed : DeepLinkEffect()
