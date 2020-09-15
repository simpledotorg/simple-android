package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.android.parcel.Parcelize
import org.intellij.lang.annotations.Language
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.storage.DaoWithUpsert
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

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
@Parcelize
data class Patient(
    @PrimaryKey
    val uuid: UUID,

    val addressUuid: UUID,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

    val status: PatientStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?,

    val recordedAt: Instant,

    val syncStatus: SyncStatus,

    val reminderConsent: ReminderConsent,

    val deletedReason: DeletedReason?,

    val registeredFacilityId: UUID?,

    val assignedFacilityId: UUID?
) : Parcelable {

  fun withNameAndGender(fullName: String, gender: Gender): Patient =
      copy(fullName = fullName, gender = gender)

  fun withoutAgeAndDateOfBirth(): Patient =
      copy(age = null, dateOfBirth = null)

  fun withAge(age: Age): Patient =
      copy(age = age)

  fun withDateOfBirth(dateOfBirth: LocalDate): Patient =
      copy(dateOfBirth = dateOfBirth)

  @Dao
  abstract class RoomDao : DaoWithUpsert<Patient>() {

    @Query("SELECT * FROM patient")
    abstract fun allPatients(): Flowable<List<Patient>>

    @Query("SELECT * FROM patient WHERE uuid = :uuid")
    abstract fun getOne(uuid: UUID): Patient?

    @Query("SELECT uuid FROM Patient WHERE syncStatus = :syncStatus")
    abstract fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    // Only if Room supported custom adapters, we wouldn't need both getOne() and patient().
    @Query("SELECT * FROM patient WHERE uuid = :uuid")
    abstract fun patient(uuid: UUID): Flowable<List<Patient>>

    @Query("SELECT * FROM patient WHERE uuid = :uuid LIMIT 1")
    abstract fun patientImmediate(uuid: UUID): Patient?

    fun save(patient: Patient) {
      save(listOf(patient))
    }

    fun save(patients: List<Patient>) {
      upsert(patients)
    }

    @Query("UPDATE patient SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    abstract fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus)

    @Query("UPDATE patient SET syncStatus = :newStatus WHERE uuid IN (:uuids)")
    abstract fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus)

    @Query("SELECT COUNT(uuid) FROM patient")
    abstract fun patientCount(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM Patient WHERE syncStatus = :syncStatus")
    abstract fun patientCount(syncStatus: SyncStatus): Flowable<Int>

    @Query("DELETE FROM patient")
    abstract fun clear()

    @Query("""
      SELECT P.* FROM Patient P
      INNER JOIN BusinessId B ON B.patientUuid == P.uuid
      WHERE 
        (P.deletedAt IS NULL) AND
        (B.identifier == :identifier AND B.deletedAt IS NULL)
      ORDER BY B.createdAt ASC
    """)
    abstract fun findPatientsWithBusinessId(identifier: String): Flowable<List<Patient>>

    @Query("""
      UPDATE patient
      SET status = :newStatus, syncStatus = :newSyncStatus, updatedAt = :newUpdatedAt
      WHERE uuid = :uuid
      """)
    abstract fun updatePatientStatus(
        uuid: UUID,
        newStatus: PatientStatus,
        newSyncStatus: SyncStatus,
        newUpdatedAt: Instant
    )

    @Query("""
      UPDATE Patient
      SET recordedAt = :instantToCompare, syncStatus = :pendingStatus, updatedAt = :updatedAt
      WHERE uuid = :patientUuid AND recordedAt > :instantToCompare
    """)
    abstract fun compareAndUpdateRecordedAt(
        patientUuid: UUID,
        instantToCompare: Instant,
        updatedAt: Instant,
        pendingStatus: SyncStatus
    )

    @Query("""
      UPDATE Patient
      SET
        recordedAt = MIN(
          createdAt,
          IFNULL(
            (SELECT recordedAt
            FROM BloodPressureMeasurement
            WHERE patientUuid = :patientUuid AND deletedAt IS NULL
            ORDER BY recordedAt ASC LIMIT 1),
            createdAt
          )
        ),
        updatedAt = :updatedAt,
        syncStatus = :pendingStatus
      WHERE uuid = :patientUuid
    """)
    abstract fun updateRecordedAt(
        patientUuid: UUID,
        updatedAt: Instant,
        pendingStatus: SyncStatus
    )

    // Patient can have multiple phone numbers, and Room's support for @Relation annotations doesn't
    // support loading into constructor parameters and needs a settable property. Room does fix
    // this limitation in 2.1.0, but it requires migration to AndroidX. For now, we create a
    // transient query model whose only job is to represent this and process it in memory.
    // TODO: Remove this when we migrate to Room 2.1.0.
    @Query("$patientProfileQuery WHERE P.syncStatus == :syncStatus")
    protected abstract fun loadPatientQueryModelsWithSyncStatus(syncStatus: SyncStatus): List<PatientQueryModel>

    @Query("$patientProfileQuery WHERE P.uuid == :patientUuid")
    protected abstract fun loadPatientQueryModelsForPatientUuid(patientUuid: UUID): Flowable<List<PatientQueryModel>>

    @Query("$patientProfileQuery WHERE P.uuid == :patientUuid")
    protected abstract fun loadPatientQueryModelsForPatientUuidImmediate(patientUuid: UUID): List<PatientQueryModel>

    @Query("""
      UPDATE Patient
      SET
        updatedAt = :updatedAt,
        deletedAt = :deletedAt,
        deletedReason = :deletedReason,
        syncStatus = :pendingStatus
      WHERE uuid = :patientUuid
    """)
    abstract fun deletePatient(
        patientUuid: UUID,
        updatedAt: Instant,
        deletedAt: Instant,
        deletedReason: DeletedReason,
        pendingStatus: SyncStatus
    )

    @Query("""
      UPDATE Patient
      SET
        assignedFacilityId = :assignedFacilityId,
        updatedAt = :updatedAt,
        syncStatus = :pendingStatus
      WHERE uuid = :patientUuid
    """)
    abstract fun updateAssignedFacilityId(
        patientUuid: UUID,
        assignedFacilityId: UUID,
        updatedAt: Instant,
        pendingStatus: SyncStatus
    )

    fun recordsWithSyncStatus(syncStatus: SyncStatus): List<PatientProfile> {
      return queryModelsToPatientProfiles(loadPatientQueryModelsWithSyncStatus(syncStatus))
    }

    fun patientProfile(patientUuid: UUID): Observable<Optional<PatientProfile>> {
      return loadPatientQueryModelsForPatientUuid(patientUuid)
          .map { queryModelsToPatientProfiles(it) }
          .map { if (it.isEmpty()) None<PatientProfile>() else Just(it.first()) }
          .toObservable()
    }

    fun patientProfileImmediate(patientUuid: UUID): PatientProfile? {
      val patientQueryModels = loadPatientQueryModelsForPatientUuidImmediate(patientUuid)

      return if (patientQueryModels.isNotEmpty()) queryModelsToPatientProfiles(patientQueryModels).first() else null
    }

    private fun queryModelsToPatientProfiles(patientQueryModels: List<PatientQueryModel>): List<PatientProfile> {
      return patientQueryModels
          .groupBy { it.patient.uuid }
          .map { (_, patientQueryModels) ->
            val patient = patientQueryModels.first().patient
            val patientAddress = patientQueryModels.first().address

            val patientPhoneNumbers = patientQueryModels
                .filter { it.phoneNumber != null }
                .map { it.phoneNumber as PatientPhoneNumber }
                .distinctBy { it.uuid }
                .toList()

            val businessIds = patientQueryModels
                .filter { it.businessId != null }
                .map { it.businessId as BusinessId }
                .distinctBy { it.uuid }
                .toList()

            PatientProfile(
                patient = patient,
                address = patientAddress,
                phoneNumbers = patientPhoneNumbers,
                businessIds = businessIds
            )
          }
    }

    protected data class PatientQueryModel(

        @Embedded(prefix = "patient_")
        val patient: Patient,

        @Embedded(prefix = "addr_")
        val address: PatientAddress,

        @Embedded(prefix = "phone_")
        val phoneNumber: PatientPhoneNumber?,

        @Embedded(prefix = "businessid_")
        val businessId: BusinessId?
    )

    @Query("""
          SELECT (
            CASE
              WHEN
                (BP.systolic >= 140 OR BP.diastolic >= 90
                OR (BloodSugar.type = "random" AND BloodSugar.value >= 300)
                OR (BloodSugar.type = "fasting" AND BloodSugar.value >= 200)
                OR (BloodSugar.type = "hba1c" AND BloodSugar.value >= 9)
                OR MH.hasHadHeartAttack = :yesAnswer
                OR MH.hasHadStroke = :yesAnswer
                OR PD.uuid IS NOT NULL)
                AND A.uuid IS NULL
                THEN 1
                ELSE 0
            END
          ) AS isPatientDefaulter
          FROM Patient P
          LEFT JOIN (SELECT BP.systolic, BP.diastolic FROM BloodPressureMeasurement BP WHERE BP.deletedAt IS NULL AND BP.patientUuid = :patientUuid ORDER BY BP.recordedAt DESC LIMIT 1) BP
          LEFT JOIN (SELECT BloodSugar.reading_value value, BloodSugar.reading_type type FROM BloodSugarMeasurements BloodSugar WHERE BloodSugar.deletedAt IS NULL AND BloodSugar.patientUuid = :patientUuid ORDER BY BloodSugar.recordedAt DESC LIMIT 1) BloodSugar
          LEFT JOIN (SELECT MH.hasHadHeartAttack, MH.hasHadStroke, MH.hasDiabetes, MH.hasHadKidneyDisease FROM MedicalHistory MH WHERE MH.deletedAt IS NULL AND MH.patientUuid = :patientUuid ORDER BY MH.updatedAt DESC LIMIT 1) MH
          LEFT JOIN (SELECT PD.uuid FROM PrescribedDrug PD WHERE PD.deletedAt IS NULL AND PD.patientUuid = :patientUuid ORDER BY PD.updatedAt DESC LIMIT 1) PD
          LEFT JOIN Appointment A ON(A.patientUuid = P.uuid AND A.deletedAt IS NULL AND A.status = :scheduled)
          WHERE P.uuid = :patientUuid AND P.deletedAt IS NULL
    """)
    abstract fun isPatientDefaulter(
        patientUuid: UUID,
        yesAnswer: Answer = Answer.Yes,
        scheduled: Appointment.Status = Appointment.Status.Scheduled
    ): Boolean

    @Query("""
        SELECT (
            CASE
                WHEN (COUNT(uuid) > 0) THEN 1
                ELSE 0
            END
        )
        FROM Patient
        WHERE updatedAt > :instantToCompare AND syncStatus = :pendingStatus AND uuid = :patientUuid
    """)
    abstract fun hasPatientChangedSince(
        patientUuid: UUID,
        instantToCompare: Instant,
        pendingStatus: SyncStatus
    ): Boolean

    companion object {
      @Language("RoomSql")
      const val patientProfileQuery = """
        SELECT
          P.uuid patient_uuid, P.addressUuid patient_addressUuid, P.fullName patient_fullName,
          P.gender patient_gender, P.dateOfBirth patient_dateOfBirth,
          P.age_value patient_age_value, P.age_updatedAt patient_age_updatedAt, P.status patient_status,
          P.createdAt patient_createdAt, P.updatedAt patient_updatedAt, P.deletedAt patient_deletedAt,
          P.syncStatus patient_syncStatus, P.recordedAt patient_recordedAt, P.reminderConsent patient_reminderConsent, P.deletedReason patient_deletedReason,
          P.registeredFacilityId patient_registeredFacilityId, P.assignedFacilityId patient_assignedFacilityId,

          PA.uuid addr_uuid, PA.colonyOrVillage addr_colonyOrVillage, PA.district addr_district,
          PA.state addr_state, PA.country addr_country,
          PA.createdAt addr_createdAt,PA.updatedAt addr_updatedAt, PA.deletedAt addr_deletedAt,
          PA.streetAddress addr_streetAddress, PA.zone addr_zone,

          PPN.uuid phone_uuid, PPN.patientUuid phone_patientUuid, PPN.number phone_number,
          PPN.phoneType phone_phoneType, PPN.active phone_active,
          PPN.createdAt phone_createdAt, PPN.updatedAt phone_updatedAt, PPN.deletedAt phone_deletedAt,

          BI.uuid businessid_uuid, BI.patientUuid businessid_patientUuid, BI.identifier businessid_identifier,
          BI.identifierType businessid_identifierType, BI.meta businessid_meta, BI.metaVersion businessid_metaVersion,
          BI.createdAt businessid_createdAt, BI.updatedAt businessid_updatedAt, BI.deletedAt businessid_deletedAt
        FROM Patient P
        INNER JOIN PatientAddress PA ON P.addressUuid == PA.uuid
        LEFT JOIN PatientPhoneNumber PPN ON PPN.patientUuid == P.uuid
        LEFT JOIN BusinessId BI ON BI.patientUuid == P.uuid
      """
    }

    // This depends on the foreign key references between address, patient
    // phone numbers, and business IDs to cascade the deletes.
    @Query("""
      DELETE FROM PatientAddress
      WHERE uuid IN (
        SELECT addressUuid FROM Patient
        WHERE deletedAt IS NOT NULL AND syncStatus == 'DONE'
      )
    """)
    abstract fun purgeDeleted()

    @Query("""
      DELETE FROM PatientPhoneNumber
      WHERE uuid IN (
        SELECT PPN.uuid FROM PatientPhoneNumber PPN
        INNER JOIN Patient P ON P.uuid == PPN.patientUuid
        WHERE P.syncStatus == 'DONE' AND PPN.deletedAt IS NOT NULL
      )
    """)
    abstract fun purgeDeletedPhoneNumbers()
  }
}
