package org.simple.clinic.facility

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
data class Facility(

    @PrimaryKey
    val uuid: UUID,

    val name: String,

    val facilityType: String?,

    val streetAddress: String?,

    val villageOrColony: String?,

    val district: String,

    val state: String,

    val country: String = "India",

    val pinCode: String?,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus
) {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM facility WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<Facility>>

    @Query("UPDATE facility SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE facility SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(newFacilities: List<Facility>)

    @Query("SELECT * FROM facility WHERE uuid = :uuid LIMIT 1")
    fun getOne(uuid: UUID): Facility?

    @Query("SELECT * FROM facility ORDER BY name ASC")
    fun facilities(): Flowable<List<Facility>>

    @Query("SELECT COUNT(uuid) FROM facility")
    fun count(): Flowable<Int>
  }
}
