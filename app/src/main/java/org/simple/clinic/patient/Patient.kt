package org.simple.clinic.patient

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

/**
 * [Regex] for stripping patient names and search queries of white spaces and punctuation
 *
 * Currently matches the following characters
 * - Any whitespace
 * - Comma, Hyphen, SemiColon, Colon, Underscore, Apostrophe, Period
 * */
private val spacePunctuationRegex = Regex("[\\s;_\\-:,'\\\\.]")

fun nameToSearchableForm(string: String) = string.replace(spacePunctuationRegex, "")

@Entity(
    foreignKeys = [
      ForeignKey(
          entity = PatientAddress::class,
          parentColumns = ["uuid"],
          childColumns = ["addressUuid"],
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE)
    ],
    indices = [
      Index("addressUuid", unique = true)
    ])
data class Patient constructor(
    @PrimaryKey
    val uuid: UUID,

    val addressUuid: UUID,

    val fullName: String,

    val searchableName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

    val status: PatientStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus
) {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM patient")
    fun allPatients(): Flowable<List<Patient>>

    @Query("SELECT * FROM patient WHERE uuid = :uuid")
    fun getOne(uuid: UUID): Patient?

    // Only if Room supported custom adapters, we wouldn't need both getOne() and patient().
    @Query("SELECT * FROM patient WHERE uuid = :uuid")
    fun patient(uuid: UUID): Flowable<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(patient: Patient)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(patient: List<Patient>)

    @Query("UPDATE patient SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE patient SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Query("SELECT COUNT(uuid) FROM patient")
    fun patientCount(): Flowable<Int>

    @Query("DELETE FROM patient")
    fun clear()
  }
}
