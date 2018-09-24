package org.simple.clinic.overdue.communication

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
    val updatedAt: Instant
) {

  enum class Type {
    @Json(name = "manual_call")
    MANUAL_CALL,

    @Json(name = "voip_call")
    VOIP_CALL;

    class RoomTypeConverter : RoomEnumTypeConverter<Type>(Type::class.java)
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

    @Query("DELETE FROM Communication")
    fun clear()
  }
}
