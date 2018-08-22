package org.simple.clinic.overdue

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.simple.clinic.util.RoomEnumTypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@Entity(tableName = "FollowUpSchedule")
data class FollowUpSchedule(
    @PrimaryKey val id: UUID,
    val patientId: UUID,
    val facilityId: UUID,
    val nextVisit: LocalDate,
    val userAction: UserAction,
    val actionByUserId: UUID,
    val reasonToAction: UserActionReason,
    val createdAt: Instant,
    val updatedAt: Instant
) {

  enum class UserAction {
    SCHEDULED,
    SKIPPED,
    CALL_AGAIN;

    class RoomTypeConverter : RoomEnumTypeConverter<UserAction>(UserAction::class.java)
  }

  enum class UserActionReason {
    PATIENT_ALREADY_VISITED_CLINIC,
    PATIENT_MOVED_OUT_OF_AREA,
    PATIENT_NOT_RESPONDING,
    PATIENT_DIED,
    OTHER;

    class RoomTypeConverter : RoomEnumTypeConverter<UserActionReason>(UserActionReason::class.java)
  }
}
