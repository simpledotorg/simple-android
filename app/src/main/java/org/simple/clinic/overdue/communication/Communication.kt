package org.simple.clinic.overdue.communication

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson
import io.reactivex.Flowable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RoomEnumTypeConverter
import org.simple.clinic.util.SafeEnumTypeAdapter
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "Communication")
data class Communication(
    @PrimaryKey val uuid: UUID,
    val appointmentUuid: UUID,
    val userUuid: UUID,
    val type: Type,
    val result: Result,
    val syncStatus: SyncStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
) {

  sealed class Type {

    object ManualCall : Type()

    object VoipCall : Type()

    object MissedVisitSmsReminder : Type()

    data class Unknown(val actual: String) : Type()

    object TypeAdapter : SafeEnumTypeAdapter<Type>(
        knownMappings = mapOf(
            ManualCall to "manual_call",
            VoipCall to "voip_call",
            MissedVisitSmsReminder to "missed_visit_sms_reminder"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actual }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(enum: Type?) = TypeAdapter.fromEnum(enum)
    }

    class MoshiTypeAdapter {

      @FromJson
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(enum: Type?) = TypeAdapter.fromEnum(enum)
    }

    companion object {
      @VisibleForTesting(otherwise = VisibleForTesting.NONE)
      fun random() = TypeAdapter.knownMappings.keys.shuffled().first()
    }
  }

  enum class Result {
    @Json(name = "unavailable")
    UNAVAILABLE,

    @Json(name = "unreachable")
    UNREACHABLE,

    @Json(name = "successful")
    SUCCESSFUL;

    class RoomTypeConverter : RoomEnumTypeConverter<Result>(Result::class.java)
  }

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM Communication WHERE syncStatus = :status")
    fun recordsWithSyncStatus(status: SyncStatus): Flowable<List<Communication>>

    @Query("UPDATE Communication SET syncStatus = :to WHERE syncStatus = :from")
    fun updateSyncStatus(from: SyncStatus, to: SyncStatus)

    @Query("UPDATE Communication SET syncStatus = :to WHERE uuid IN (:ids)")
    fun updateSyncStatus(ids: List<UUID>, to: SyncStatus)

    @Query("SELECT * FROM Communication WHERE uuid = :id LIMIT 1")
    fun getOne(id: UUID): Communication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(schedules: List<Communication>)

    @Query("SELECT COUNT(uuid) FROM Communication")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM Communication WHERE syncStatus = :syncStatus")
    fun count(syncStatus: SyncStatus): Flowable<Int>

    @Query("DELETE FROM Communication")
    fun clear()
  }
}
