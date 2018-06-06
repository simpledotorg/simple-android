package org.resolvetosavelives.red.patient

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import org.threeten.bp.Instant
import java.util.UUID

@Entity
data class PatientAddress(
    @PrimaryKey
    val uuid: UUID,

    val colonyOrVillage: String?,

    val district: String,

    val state: String,

    // TODO: Don't use India as the country for everyone!
    val country: String? = "India",

    val createdAt: Instant,

    val updatedAt: Instant
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(address: PatientAddress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(address: List<PatientAddress>)

    @Query("SELECT * FROM patientaddress WHERE uuid = :uuid LIMIT 1")
    fun get(uuid: UUID): PatientAddress?
  }
}
