package org.simple.clinic.overdue

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import com.squareup.moshi.Json
import io.reactivex.Flowable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RoomEnumTypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@Entity(tableName = "Appointment")
data class Appointment(

    @ColumnInfo(name = "id")
    @PrimaryKey
    val uuid: UUID,

    @ColumnInfo(name = "patientId")
    val patientUuid: UUID,

    @ColumnInfo(name = "facilityId")
    val facilityUuid: UUID,

    val date: LocalDate,

    val status: Status,

    val statusReason: StatusReason,

    val syncStatus: SyncStatus,

    val createdAt: Instant,

    val updatedAt: Instant
) {

  enum class Status {

    @Json(name = "scheduled")
    SCHEDULED,

    @Json(name = "cancelled")
    CANCELLED,

    @Json(name = "visited")
    VISITED;

    class RoomTypeConverter : RoomEnumTypeConverter<Status>(Status::class.java)
  }

  enum class StatusReason {

    @Json(name = "not_called_yet")
    NOT_CALLED_YET,

    @Json(name = "not_responding")
    PATIENT_NOT_RESPONDING,

    @Json(name = "moved")
    MOVED,

    @Json(name = "dead")
    DEAD;

    class RoomTypeConverter : RoomEnumTypeConverter<StatusReason>(StatusReason::class.java)
  }

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM Appointment WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<Appointment>>

    @Query("UPDATE Appointment SET syncStatus = :to WHERE syncStatus = :from")
    fun updateSyncStatus(from: SyncStatus, to: SyncStatus)

    @Query("UPDATE Appointment SET syncStatus = :to WHERE id IN (:ids)")
    fun updateSyncStatus(ids: List<UUID>, to: SyncStatus)

    @Query("SELECT * FROM Appointment WHERE id = :id LIMIT 1")
    fun getOne(id: UUID): Appointment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(appointments: List<Appointment>)

    @Query("SELECT COUNT(id) FROM Appointment")
    fun count(): Int

    @Query("SELECT * FROM Appointment")
    fun appointments(): Flowable<List<Appointment>>
  }
}
