package org.simple.clinic.deeplink

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

sealed class DeepLinkResult : Parcelable

@Parcelize
data class OpenPatientSummary(val patientUuid: UUID) : DeepLinkResult()

@Parcelize
object ShowPatientNotFound : DeepLinkResult()
