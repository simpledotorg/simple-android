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
      Index("addressUuid")
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

    @Query("UPDATE patient SET status = :newStatus WHERE uuid = :uuid")
    fun updatePatientStatus(uuid: UUID, newStatus: PatientStatus)

    @Query("""
      SELECT P.uuid patient_uuid, P.addressUuid patient_addressUuid, P.fullName patient_fullName, P.searchableName patient_searchableName,
       P.gender patient_gender, P.dateOfBirth patient_dateOfBirth, P.age_value patient_age_value, P.age_updatedAt patient_age_updatedAt,
       P.age_computedDateOfBirth patient_age_computedDateOfBirth, P.status patient_status, P.createdAt patient_createdAt,
       P.updatedAt patient_updatedAt, P.syncStatus patient_syncStatus, PA.uuid addr_uuid, PA.colonyOrVillage addr_colonyOrVillage,
       PA.district addr_district, PA.state addr_state, PA.country addr_country, PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt,
       PPN.uuid phone_uuid, PPN.patientUuid phone_patientUuid, PPN.number phone_number, PPN.phoneType phone_phoneType, PPN.active phone_active,
       PPN.createdAt phone_createdAt, PPN.updatedAt phone_updatedAt
      FROM Patient P
      INNER JOIN PatientAddress PA ON P.addressUuid == PA.uuid
      LEFT JOIN PatientPhoneNumber PPN ON PPN.patientUuid == P.uuid
      WHERE P.syncStatus == :syncStatus
    """)
    fun recordsWithSyncStatus(syncStatus: SyncStatus): Flowable<List<PatientQueryModel>>
  }
}
