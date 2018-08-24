package org.simple.clinic.overdue

import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

data class FollowUpSchedule(
    @PrimaryKey val id: UUID,
    val patientId: UUID,
    val facilityId: UUID,
    val nextVisit: LocalDate,
    val userAction: UserAction,
    val actionByUserId: UUID,
    val reasonForAction: UserActionReason,
    val syncStatus: SyncStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {

  enum class UserAction {

    @Json(name = "scheduled")
    SCHEDULED,

    @Json(name = "skipped")
    SKIPPED;
  }

  enum class UserActionReason {

    @Json(name = "already_visited")
    PATIENT_ALREADY_VISITED_CLINIC,

    @Json(name = "not_responding")
    PATIENT_NOT_RESPONDING;
  }
}
