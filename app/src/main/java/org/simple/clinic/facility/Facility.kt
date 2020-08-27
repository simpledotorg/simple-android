package org.simple.clinic.facility

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.location.Coordinates
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID

@Entity
@Parcelize
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

    val deletedAt: Instant?,

    @Embedded(prefix = "config_")
    val config: FacilityConfig
) : Parcelable {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM facility WHERE syncStatus = :status")
    fun withSyncStatus(status: SyncStatus): List<Facility>

    @Query("UPDATE facility SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE facility SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(newFacilities: List<Facility>)

    @Query("SELECT * FROM facility WHERE uuid = :uuid LIMIT 1")
    fun getOne(uuid: UUID): Facility?

    @Query("SELECT uuid FROM facility WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Query("SELECT * FROM facility WHERE deletedAt IS NULL ORDER BY name ASC")
    fun all(): Flowable<List<Facility>>

    @Query("""
      SELECT * FROM facility
      WHERE name LIKE '%' || :nameFilter || '%' AND deletedAt IS NULL
      ORDER BY name ASC
    """)
    fun filteredByName(nameFilter: String): Flowable<List<Facility>>

    @Query("""
      SELECT * FROM facility
      WHERE name LIKE '%' || :nameFilter || '%' AND groupUuid = :groupUuid
      AND deletedAt IS NULL
      ORDER BY name ASC
    """)
    fun filteredByNameAndGroup(nameFilter: String, groupUuid: UUID): Flowable<List<Facility>>

    @Query("""
      SELECT * FROM facility
      WHERE groupUuid = :groupUuid AND deletedAt IS NULL
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
