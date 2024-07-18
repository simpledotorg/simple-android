package org.simple.clinic.deeplink

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

sealed class DeepLinkResult : Parcelable

@Parcelize
data class OpenPatientSummary(val patientUuid: UUID) : DeepLinkResult()

@Parcelize
data object ShowPatientNotFound : DeepLinkResult()

@Parcelize
data object ShowNoPatientUuid : DeepLinkResult()

@Parcelize
data class OpenPatientSummaryWithTeleconsultLog(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : DeepLinkResult()

@Parcelize
data object ShowTeleconsultNotAllowed : DeepLinkResult()
