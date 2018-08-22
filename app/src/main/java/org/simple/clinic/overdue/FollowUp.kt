package org.simple.clinic.overdue

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.simple.clinic.util.RoomEnumTypeConverter
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "FollowUp")
data class FollowUp(
    @PrimaryKey val id: UUID,
    val followUpScheduleId: UUID,
    val followUpType: Type,
    val userId: UUID,
    val result: Result,
    val createdAt: Instant,
    val updatedAt: Instant
) {

  enum class Type {
    CALL,
    SMS,
    IN_PERSON;

    class RoomTypeConverter : RoomEnumTypeConverter<Type>(Type::class.java)
  }

  enum class Result {
    VISITED,
    NO_RESPONSE,
    INVALID_PHONE_NUMBER,
    UNREACHABLE;

    class RoomTypeConverter : RoomEnumTypeConverter<Result>(Result::class.java)
  }
}
