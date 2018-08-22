package org.simple.clinic.overdue

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import com.squareup.moshi.Json
import io.reactivex.Flowable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.Synceable
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
    val reasonForAction: UserActionReason,
    val syncStatus: SyncStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) : Synceable {

  enum class UserAction {

    @Json(name = "scheduled")
    SCHEDULED,

    @Json(name = "skipped")
    SKIPPED;

    class RoomTypeConverter : RoomEnumTypeConverter<UserAction>(UserAction::class.java)
  }

  enum class UserActionReason {

    @Json(name = "already_visited")
    PATIENT_ALREADY_VISITED_CLINIC,

    @Json(name = "not_responding")
    PATIENT_NOT_RESPONDING;

    class RoomTypeConverter : RoomEnumTypeConverter<UserActionReason>(UserActionReason::class.java)
  }

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM FollowUpSchedule WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<FollowUpSchedule>>

    @Query("UPDATE FollowUpSchedule SET syncStatus = :to WHERE syncStatus = :from")
    fun updateSyncStatus(from: SyncStatus, to: SyncStatus)

    @Query("UPDATE FollowUpSchedule SET syncStatus = :to WHERE id IN (:ids)")
    fun updateSyncStatus(ids: List<UUID>, to: SyncStatus)

    @Query("SELECT * FROM FollowUpSchedule WHERE id = :id LIMIT 1")
    fun getOne(id: UUID): FollowUpSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(schedules: List<FollowUpSchedule>)

    @Query("SELECT COUNT(*) FROM FollowUpSchedule")
    fun count(): Int
  }
}
