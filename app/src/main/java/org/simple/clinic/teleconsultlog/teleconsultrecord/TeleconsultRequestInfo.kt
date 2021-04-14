package org.simple.clinic.teleconsultlog.teleconsultrecord

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID

@Parcelize
data class TeleconsultRequestInfo(
    val requesterId: UUID,

    val facilityId: UUID,

    val requestedAt: Instant,

    val requesterCompletionStatus: TeleconsultStatus?
) : Parcelable
