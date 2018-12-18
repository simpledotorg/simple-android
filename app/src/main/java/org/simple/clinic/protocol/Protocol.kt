package org.simple.clinic.protocol

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

@Entity
data class Protocol(

    @PrimaryKey
    val uuid: UUID,

    val name: String,

    val followUpDays: Int,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus,

    val deletedAt: Instant?
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(protocol: List<Protocol>)

    @Query("SELECT * FROM Protocol WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<Protocol>>

    @Query("UPDATE Protocol SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE Protocol SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Query("SELECT COUNT(uuid) FROM Protocol")
    fun count(): Flowable<Int>

    @Query("SELECT * FROM Protocol WHERE uuid = :uuid")
    fun getOne(uuid: UUID): Protocol?
  }
}
