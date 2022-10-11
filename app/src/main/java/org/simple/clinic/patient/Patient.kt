package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.intellij.lang.annotations.Language
import org.simple.clinic.contactpatient.ContactPatientProfile
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.storage.DaoWithUpsert
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
      Index("addressUuid", unique = false),
      Index("assignedFacilityId", unique = false)
    ])
@Parcelize
data class Patient(
    @PrimaryKey
    val uuid: UUID,

    val addressUuid: UUID,

    val fullName: String,

    val gender: Gender,

    @Embedded
    val ageDetails: PatientAgeDetails,

    val status: PatientStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?,

    val recordedAt: Instant,

    val syncStatus: SyncStatus,

    val reminderConsent: ReminderConsent,

    val deletedReason: DeletedReason?,

    val registeredFacilityId: UUID?,

    val assignedFacilityId: UUID?,

    val retainUntil: Instant?
) : Parcelable {

  fun withNameAndGender(fullName: String, gender: Gender): Patient =
      copy(fullName = fullName, gender = gender)

  fun withDateOfBirth(dateOfBirth: LocalDate): Patient = copy(ageDetails = ageDetails.withDateOfBirth(dateOfBirth))

  fun withAgeDetails(ageDetails: PatientAgeDetails): Patient = copy(ageDetails = ageDetails)

  @Dao
  abstract class RoomDao : DaoWithUpsert<Patient>() {

    companion object {

      @Language("RoomSql")
      private const val PATIENT_LINE_LIST_QUERY = """
        SELECT 
          P.fullName patientName,
          P.gender gender,
          P.status status,
          P.age_value,
          P.age_updatedAt,
          P.dateOfBirth,
          P.createdAt registrationDate,
          RF.uuid registrationFacilityId,
          RF.name registrationFacilityName,
          AF.uuid assignedFacilityId,
          AF.name assignedFacilityName,
          PA.streetAddress streetAddress,
          PA.colonyOrVillage colonyOrVillage,
          PPN.number patientPhoneNumber,
          MH.diagnosedWithHypertension diagnosedWithHypertension,
          MH.hasDiabetes diagnosedWithDiabetes,
  
          BP.uuid bp_uuid, BP.systolic bp_systolic, BP.diastolic bp_diastolic, BP.syncStatus bp_syncStatus,
          BP.userUuid bp_userUuid, BP.facilityUuid bp_facilityUuid, BP.patientUuid bp_patientUuid, BP.createdAt bp_createdAt,
          BP.updatedAt bp_updatedAt, BP.deletedAt bp_deletedAt, BP.recordedAt bp_recordedAt,
          
          BI.uuid bp_passport_uuid, BI.patientUuid bp_passport_patientUuid,
          BI.identifier bp_passport_identifier, BI.identifierType bp_passport_identifierType,
          BI.meta bp_passport_meta, BI.metaVersion bp_passport_metaVersion, BI.createdAt bp_passport_createdAt,
          BI.updatedAt bp_passport_updatedAt, BI.deletedAt bp_passport_deletedAt, BI.searchHelp bp_passport_searchHelp

        FROM Patient P
        LEFT JOIN PatientAddress PA ON PA.uuid = P.addressUuid
        LEFT JOIN (
          SELECT * FROM PatientPhoneNumber
          GROUP BY patientUuid HAVING MAX(createdAt)
        ) PPN on PPN.patientUuid = P.uuid AND PPN.deletedAt IS NULL
        LEFT JOIN Facility RF ON RF.uuid = P.registeredFacilityId
        LEFT JOIN Facility AF ON AF.uuid = P.registeredFacilityId
        LEFT JOIN MedicalHistory MH ON MH.patientUuid = P.uuid
        LEFT JOIN (
          SELECT * FROM BloodPressureMeasurement 
          WHERE  deletedAt IS NULL
          GROUP BY patientUuid HAVING MAX(createdAt)
        ) BP ON (
          BP.patientUuid = P.uuid AND
          BP.createdAt >= :bpCreatedAfter AND
          BP.createdAt <= :bpCreateBefore
        )
        LEFT JOIN (
          SELECT * FROM BusinessId
          WHERE deletedAt IS NULL
          GROUP BY patientUuid HAVING MAX(createdAt)
        ) BI ON (
          BI.patientUuid = P.uuid AND
          BI.identifierType = "simple_bp_passport"
        )

        WHERE registeredFacilityId = :facilityId OR assignedFacilityId = :facilityId
      """
    }

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
    abstract fun updateSyncStatusForIds(uuids: List<UUID>, newStatus: SyncStatus)

    @Query("SELECT COUNT(uuid) FROM patient")
    abstract fun patientCount(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM Patient WHERE syncStatus = :syncStatus")
    abstract fun patientCountWithStatus(syncStatus: SyncStatus): Flowable<Int>

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
      SELECT P.* FROM Patient P
      INNER JOIN BusinessId B ON B.patientUuid == P.uuid
      WHERE 
        (P.deletedAt IS NULL) AND
        (B.identifier == :identifier AND B.deletedAt IS NULL)
      ORDER BY B.createdAt ASC
    """)
    abstract fun findPatientsWithBusinessIdImmediate(identifier: String): List<Patient>

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

    @Transaction
    @Query("""
      SELECT * FROM Patient
      WHERE syncStatus = :syncStatus
    """)
    abstract fun recordsWithSyncStatus(syncStatus: SyncStatus): List<PatientProfile>

    @Transaction
    @Query("""
      SELECT * FROM Patient
      WHERE uuid = :patientUuid
    """)
    abstract fun patientProfile(patientUuid: UUID): Observable<List<PatientProfile>>

    @Transaction
    @Query("""
      SELECT * FROM Patient
      WHERE uuid = :patientUuid
    """)
    abstract fun patientProfileImmediate(patientUuid: UUID): PatientProfile?

    @Transaction
    @Query("SELECT * FROM Patient")
    abstract fun allPatientProfiles(): List<PatientProfile>

    @Transaction
    @Query("""
      SELECT * FROM Patient
      WHERE syncStatus = :syncStatus
      LIMIT :limit OFFSET :offset
    """)
    abstract fun profilesWithSyncStatusBatched(
        syncStatus: SyncStatus,
        limit: Int,
        offset: Int
    ): List<PatientProfile>

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

    @Query("""
      DELETE FROM BusinessId
      WHERE uuid IN (
        SELECT BI.uuid FROM BusinessId BI
        INNER JOIN Patient P ON P.uuid == BI.patientUuid
        WHERE P.syncStatus == 'DONE' AND BI.deletedAt IS NOT NULL
      )
    """)
    abstract fun purgeDeletedBusinessIds()

    // This depends on the foreign key references between address, patient
    // phone numbers, and business IDs to cascade the deletes.
    @Query("""
      DELETE FROM PatientAddress
      WHERE uuid NOT IN (
        SELECT DISTINCT P.addressUuid FROM Patient P
        LEFT JOIN Appointment A ON A.patientUuid == P.uuid
        WHERE (
            P.registeredFacilityId IN (:facilityIds) OR
            P.assignedFacilityId IN (:facilityIds) OR
            P.syncStatus == 'PENDING'
        ) OR (
            A.facilityUuid IN (:facilityIds) AND
            A.status = 'scheduled'
        )
      )
    """)
    abstract fun deletePatientsNotInFacilities(facilityIds: List<UUID>)

    @Query("""
        DELETE FROM Patient
        WHERE retainUntil IS NOT NULL 
        AND retainUntil < :now
        AND syncStatus == 'DONE'
    """)
    abstract fun purgePatientAfterRetentionTime(now: Instant)

    @Transaction
    @Query("""
      SELECT * FROM Patient
      WHERE uuid = :patientUuid
    """)
    abstract fun contactPatientProfileImmediate(patientUuid: UUID): ContactPatientProfile

    @Query("""
      SELECT DISTINCT * FROM (
        SELECT colonyOrVillage 
        FROM PatientAddress 
        WHERE uuid IN (
          SELECT DISTINCT P.addressUuid 
          FROM Patient P    
          WHERE P.assignedFacilityId = :facilityUuid
          OR P.registeredFacilityId = :facilityUuid
        ) 
        AND colonyOrVillage IS NOT NULL

        UNION 
    
        SELECT fullName 
        FROM Patient 
        WHERE ( 
          assignedFacilityId = :facilityUuid OR   
          registeredFacilityId = :facilityUuid 
        )
        AND fullName IS NOT NULL 
      )
     ORDER BY colonyOrVillage ASC   
    """)
    abstract fun villageAndPatientNamesInFacility(facilityUuid: UUID): List<String>

    @Query("""
      $PATIENT_LINE_LIST_QUERY
      LIMIT 1000 OFFSET :offset
    """)
    abstract fun patientLineListRows(
        facilityId: UUID,
        bpCreatedAfter: LocalDate,
        bpCreateBefore: LocalDate,
        offset: Int
    ): List<PatientLineListRow>

    @Query("""
      SELECT COUNT(*) FROM ($PATIENT_LINE_LIST_QUERY)
    """)
    abstract fun patientLineListCount(
        facilityId: UUID,
        bpCreatedAfter: LocalDate,
        bpCreateBefore: LocalDate
    ): Int
  }
}

@Entity
@Fts4(contentEntity = Patient::class)
data class PatientFts(val uuid: UUID, val fullName: String)
