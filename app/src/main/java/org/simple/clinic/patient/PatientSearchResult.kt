package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import org.intellij.lang.annotations.Language
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

data class PatientSearchResult(

    val uuid: UUID,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

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

    @Embedded(prefix = "bp_")
    val lastBp: LastBp?
) {

  override fun toString(): String {
    return "Name: $fullName, UUID: $uuid, Facility UUID: ${lastBp?.takenAtFacilityUuid}"
  }

  @Dao
  interface RoomDao {

    companion object {
      @Language("RoomSql")
      const val mainQuery = """
          SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.age_computedDateOfBirth, P.status, P.createdAt, P.updatedAt, P.syncStatus, P.recordedAt,
          PA.uuid addr_uuid, PA.colonyOrVillage addr_colonyOrVillage, PA.district addr_district, PA.state addr_state, PA.country addr_country,
          PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt,
          PP.uuid phoneUuid, PP.number phoneNumber, PP.phoneType phoneType, PP.active phoneActive, PP.createdAt phoneCreatedAt, PP.updatedAt phoneUpdatedAt,
          BP.uuid bp_uuid, BP.recordedAt bp_takenOn, BP.facilityName bp_takenAtFacilityName, BP.facilityUuid bp_takenAtFacilityUuid
          FROM Patient P
          INNER JOIN PatientAddress PA ON PA.uuid = P.addressUuid
          LEFT JOIN PatientPhoneNumber PP ON PP.patientUuid = P.uuid
          LEFT JOIN (
        		SELECT BP.uuid, BP.patientUuid, BP.recordedAt, F.name facilityName, F.uuid facilityUuid
        		FROM (
                SELECT BP.uuid, BP.patientUuid, BP.recordedAt, BP.facilityUuid
                FROM BloodPressureMeasurement BP
                WHERE BP.deletedAt IS NULL
                GROUP BY BP.patientUuid HAVING MAX (BP.recordedAt)
                ORDER BY BP.recordedAt DESC
            ) BP
        		INNER JOIN Facility F ON BP.facilityUuid = F.uuid
        	) BP ON (BP.patientUuid = P.uuid)
    """
    }

    @Query("""$mainQuery WHERE P.uuid IN (:uuids) AND P.status = :status AND P.deletedAt IS NULL""")
    fun searchByIds(uuids: List<UUID>, status: PatientStatus): Single<List<PatientSearchResult>>

    @Query("""SELECT Patient.uuid, Patient.fullName FROM Patient WHERE Patient.status = :status AND Patient.deletedAt IS NULL""")
    fun nameAndId(status: PatientStatus): Flowable<List<PatientNameAndId>>

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("""
      SELECT * FROM ($mainQuery) AllSearchResults
      INNER JOIN (
        SELECT DISTINCT P.uuid FROM Patient P
          INNER JOIN BloodPressureMeasurement BP ON BP.patientUuid = P.uuid
          WHERE BP.deletedAt IS NULL AND P.status = :status AND BP.facilityUuid = :facilityUuid
      ) PatientsAtFacility ON AllSearchResults.uuid = PatientsAtFacility.uuid
      ORDER BY AllSearchResults.fullName COLLATE NOCASE ASC
    """)
    fun searchInFacilityAndSortByName(facilityUuid: UUID, status: PatientStatus): Flowable<List<PatientSearchResult>>

    @Suppress("AndroidUnresolvedRoomSqlReference")
    @Query("""
        $mainQuery WHERE phoneNumber LIKE '%' || :phoneNumber || '%' AND P.deletedAt IS NULL
        ORDER BY P.fullName COLLATE NOCASE ASC
    """)
    fun searchByPhoneNumber(phoneNumber: String): Flowable<List<PatientSearchResult>>
  }

  data class PatientNameAndId(val uuid: UUID, val fullName: String)

  @Parcelize
  data class LastBp(
      val uuid: UUID,
      val takenOn: Instant,
      val takenAtFacilityName: String,
      val takenAtFacilityUuid: UUID
  ) : Parcelable
}
