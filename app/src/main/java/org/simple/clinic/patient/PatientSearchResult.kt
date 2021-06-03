package org.simple.clinic.patient

import android.os.Parcelable
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Query
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.businessid.Identifier
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@DatabaseView("""
  SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.assignedFacilityId, P.status, P.createdAt, P.updatedAt, P.syncStatus, P.recordedAt,
  PA.uuid addr_uuid, PA.streetAddress addr_streetAddress, PA.colonyOrVillage addr_colonyOrVillage, PA.zone addr_zone, PA.district addr_district,
  PA.state addr_state, PA.country addr_country,
  PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt,
  PP.uuid phoneUuid, PP.number phoneNumber, PP.phoneType phoneType, PP.active phoneActive, PP.createdAt phoneCreatedAt, PP.updatedAt phoneUpdatedAt,
  PatientLastSeen.lastSeenTime lastSeen_lastSeenOn, F.name lastSeen_lastSeenAtFacilityName, PatientLastSeen.lastSeenFacilityUuid lastSeen_lastSeenAtFacilityUuid,
  B.identifier id_identifier, B.identifierType id_identifierType, B.searchHelp identifierSearchHelp
  FROM Patient P
  INNER JOIN PatientAddress PA ON PA.uuid = P.addressUuid
  LEFT JOIN PatientPhoneNumber PP ON PP.patientUuid = P.uuid
  LEFT JOIN (
      SELECT P.uuid patientUuid,
      (
          CASE
              WHEN LatestBloodSugar.uuid IS NULL THEN LatestBP.recordedAt
              WHEN LatestBP.uuid IS NULL THEN LatestBloodSugar.recordedAt
              ELSE MAX(LatestBP.recordedAt, LatestBloodSugar.recordedAt)
          END
      ) lastSeenTime,
      (
          CASE
              WHEN LatestBloodSugar.uuid IS NULL THEN LatestBP.facilityUuid
              WHEN LatestBP.uuid IS NULL THEN LatestBloodSugar.facilityUuid
              WHEN LatestBP.recordedAt > LatestBloodSugar.recordedAt THEN LatestBP.facilityUuid
              ELSE LatestBloodSugar.facilityUuid
          END
      ) lastSeenFacilityUuid
      FROM Patient P
      LEFT JOIN (
          SELECT BP.uuid, BP.patientUuid, BP.recordedAt, F.uuid facilityUuid FROM BloodPressureMeasurement BP
          INNER JOIN Facility F ON F.uuid = BP.facilityUuid
          WHERE BP.deletedAt IS NULL
          GROUP BY BP.patientUuid HAVING MAX(BP.recordedAt)
      ) LatestBP ON LatestBP.patientUuid = P.uuid
      LEFT JOIN (
          SELECT BloodSugar.uuid, BloodSugar.patientUuid, BloodSugar.recordedAt, F.uuid facilityUuid FROM BloodSugarMeasurements BloodSugar
          INNER JOIN Facility F ON F.uuid = BloodSugar.facilityUuid
          WHERE BloodSugar.deletedAt IS NULL
          GROUP BY BloodSugar.patientUuid HAVING MAX(BloodSugar.recordedAt)
      ) LatestBloodSugar ON LatestBloodSugar.patientUuid = P.uuid
      WHERE LatestBP.uuid IS NOT NULL OR LatestBloodSugar.uuid IS NOT NULL
  ) PatientLastSeen ON PatientLastSeen.patientUuid = P.uuid
  LEFT JOIN Facility F ON F.uuid = PatientLastSeen.lastSeenFacilityUuid
  LEFT JOIN BusinessId B ON B.patientUuid = P.uuid
""")
@Parcelize
data class PatientSearchResult(

    val uuid: UUID,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

    val assignedFacilityId: UUID?,

    val status: PatientStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus,

    @Embedded(prefix = "addr_")
    val address: PatientAddress,

    // TODO: Use embedded PatientPhoneNumber instead of flattened fields.
    // https://www.pivotaltracker.com/story/show/160617492
    val phoneUuid: UUID?,

    val phoneNumber: String?,

    val phoneType: PatientPhoneNumberType?,

    val phoneActive: Boolean?,

    val phoneCreatedAt: Instant?,

    val phoneUpdatedAt: Instant?,

    @Embedded(prefix = "lastSeen_")
    val lastSeen: LastSeen?,

    @Embedded(prefix = "id_")
    val identifier: Identifier?,

    val identifierSearchHelp: String?
) : Parcelable {

  override fun toString(): String {
    return "Name: $fullName, UUID: $uuid, Facility UUID: ${lastSeen?.lastSeenAtFacilityUuid}"
  }

  @Dao
  interface RoomDao {

    @Query("""
      SELECT * FROM 
      PatientSearchResult searchResult
      LEFT JOIN Patient P ON P.uuid = searchResult.uuid
      WHERE P.status = :status AND P.deletedAt IS NULL AND P.assignedFacilityId = :facilityUuid
      ORDER BY fullName COLLATE NOCASE
    """)
    fun searchInFacilityAndSortByName(
        facilityUuid: UUID,
        status: PatientStatus
    ): List<PatientSearchResult>

    @Query("""
      SELECT * FROM 
      PatientSearchResult searchResult
      LEFT JOIN Patient P ON P.uuid = searchResult.uuid
      WHERE P.status = :status AND P.deletedAt IS NULL AND P.assignedFacilityId = :facilityUuid
      ORDER BY fullName COLLATE NOCASE
    """)
    fun allPatientsInFacility(
        facilityUuid: UUID,
        status: PatientStatus
    ): PagingSource<Int, PatientSearchResult>

    @Query("""
        SELECT 
            searchResult.*, 
            (
                CASE
                    WHEN P.assignedFacilityId = :facilityId THEN 0
                    ELSE 1
                END
            ) AS priority, 
            INSTR(lower(P.fullName), lower(:name)) namePosition FROM PatientSearchResult searchResult
        LEFT JOIN Patient P ON P.uuid = searchResult.uuid
        WHERE P.deletedAt IS NULL AND namePosition > 0
        ORDER BY priority ASC, namePosition ASC
    """)
    fun searchByName(name: String, facilityId: UUID): List<PatientSearchResult>

    @Query("""
        SELECT searchResult.*, INSTR(lower(P.fullName), lower(:name)) namePosition FROM PatientSearchResult searchResult
        LEFT JOIN Patient P ON P.uuid = searchResult.uuid
        WHERE P.deletedAt IS NULL AND namePosition > 0 AND P.assignedFacilityId == :facilityId
        ORDER BY namePosition ASC
    """)
    fun searchByNameAssignedFacility(name: String, facilityId: UUID): PagingSource<Int, PatientSearchResult>

    @Query("""
        SELECT searchResult.*, INSTR(lower(P.fullName), lower(:name)) namePosition FROM PatientSearchResult searchResult
        LEFT JOIN Patient P ON P.uuid = searchResult.uuid
        WHERE P.deletedAt IS NULL AND namePosition > 0 AND P.assignedFacilityId != :facilityId
        ORDER BY namePosition ASC
    """)
    fun searchByNameOtherFacility(name: String, facilityId: UUID): PagingSource<Int, PatientSearchResult>

    @Query("""
        SELECT 
            searchResult.*, 
            (
                CASE
                    WHEN P.assignedFacilityId = :facilityId THEN 0
                    ELSE 1
                END
            ) AS priority, 
            INSTR(phoneNumber, :phoneNumber) phoneNumberPosition FROM PatientSearchResult searchResult
        LEFT JOIN Patient P ON P.uuid = searchResult.uuid
        WHERE P.deletedAt IS NULL AND phoneNumberPosition > 0
        ORDER BY priority ASC, phoneNumberPosition ASC
    """)
    fun searchByPhoneNumber(phoneNumber: String, facilityId: UUID): List<PatientSearchResult>

    @Query("""
        SELECT DISTINCT
            searchResult.*, 
            (
                CASE
                    WHEN P.assignedFacilityId = :facilityId THEN 0
                    ELSE 1
                END
            ) AS priority,
            INSTR(phoneNumber, :numericCriteria) phoneNumberPosition, 
            INSTR(identifierSearchHelp, :numericCriteria) identifierPosition FROM PatientSearchResult searchResult
        LEFT JOIN Patient P ON P.uuid = searchResult.uuid
        WHERE P.deletedAt IS NULL AND phoneNumberPosition > 0 OR identifierPosition > 0
        GROUP BY P.uuid
        ORDER BY priority ASC, phoneNumberPosition ASC, identifierPosition ASC
        """)
    fun searchByNumericCriteria(
        numericCriteria: String,
        facilityId: UUID
    ): List<PatientSearchResult>
  }

  data class PatientNameAndId(val uuid: UUID, val fullName: String)

  @Parcelize
  data class LastSeen(
      val lastSeenOn: Instant,
      val lastSeenAtFacilityName: String,
      val lastSeenAtFacilityUuid: UUID
  ) : Parcelable
}
