package org.simple.clinic.facility

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import org.simple.clinic.location.Coordinates
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

    val country: String,

    val pinCode: String?,

    // Nullable because existing facilities will not
    // have protocol UUID until they're synced again.
    val protocolUuid: UUID?,

    // Nullable because existing facilities will not
    // have group UUID until they're synced again.
    val groupUuid: UUID?,

    @Embedded(prefix = "location_")
    val location: Coordinates?,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus,

    val deletedAt: Instant?
) {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM facility WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<Facility>>

    @Query("UPDATE facility SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE facility SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(newFacilities: List<Facility>)

    @Query("SELECT * FROM facility WHERE uuid = :uuid LIMIT 1")
    fun getOne(uuid: UUID): Facility?

    @Query("SELECT * FROM facility ORDER BY name ASC")
    fun all(): Flowable<List<Facility>>

    @Query("""
      SELECT * FROM facility
      WHERE name LIKE '%' || :nameFilter || '%'
      ORDER BY name ASC
    """)
    fun filteredByName(nameFilter: String): Flowable<List<Facility>>

    @Query("""
      SELECT * FROM facility
      WHERE name LIKE '%' || :nameFilter || '%' AND groupUuid = :groupUuid
      ORDER BY name ASC
    """)
    fun filteredByNameAndGroup(nameFilter: String, groupUuid: UUID): Flowable<List<Facility>>

    @Query("""
      SELECT * FROM facility
      WHERE groupUuid = :groupUuid
      ORDER BY name ASC
    """)
    fun filteredByGroup(groupUuid: UUID): Flowable<List<Facility>>

    @Query("SELECT COUNT(uuid) FROM facility")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM facility WHERE syncStatus = :syncStatus")
    fun count(syncStatus: SyncStatus): Flowable<Int>

    @Query("DELETE FROM Facility")
    fun clear()
  }
}
