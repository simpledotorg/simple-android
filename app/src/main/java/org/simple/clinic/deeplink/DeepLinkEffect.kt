package org.simple.clinic.deeplink

import java.util.UUID

sealed class DeepLinkEffect

object FetchUser : DeepLinkEffect()

object NavigateToSetupActivity : DeepLinkEffect()

data class FetchPatient(val patientUuid: UUID) : DeepLinkEffect()

data class NavigateToPatientSummary(val patientUuid: UUID) : DeepLinkEffect()

data class NavigateToPatientSummaryWithTeleconsultLog(val patientUuid: UUID, val teleconsultRecordId: UUID) : DeepLinkEffect()

object ShowPatientDoesNotExist : DeepLinkEffect()

// We will show this error when patient uuid is null
object ShowNoPatientUuidError : DeepLinkEffect()
