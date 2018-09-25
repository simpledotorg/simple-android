package org.simple.clinic.patient

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import org.intellij.lang.annotations.Language
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
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

  @Dao
  interface RoomDao {

    companion object {
      @Language("RoomSql")
      const val mainQuery = """
          SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.age_computedDateOfBirth, P.status, P.createdAt, P.updatedAt, P.syncStatus,
          PA.uuid addr_uuid, PA.colonyOrVillage addr_colonyOrVillage, PA.district addr_district, PA.state addr_state, PA.country addr_country,
          PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt,
          PP.uuid phoneUuid, PP.number phoneNumber, PP.phoneType phoneType, PP.active phoneActive, PP.createdAt phoneCreatedAt, PP.updatedAt phoneUpdatedAt,
          BP.createdAt bp_takenOn, BP.facilityName bp_takenAtFacilityName, BP.facilityUuid bp_takenAtFacilityUuid
          FROM Patient P
          INNER JOIN PatientAddress PA on PA.uuid = P.addressUuid
          LEFT JOIN PatientPhoneNumber PP ON PP.patientUuid = P.uuid
          LEFT JOIN (
        		SELECT BP.patientUuid, BP.createdAt, F.name facilityName, F.uuid facilityUuid
        		FROM BloodPressureMeasurement BP
        		INNER JOIN Facility F ON BP.facilityUuid = F.uuid
            GROUP BY BP.patientUuid
        		ORDER BY BP.createdAt DESC
        	) BP ON (BP.patientUuid = P.uuid)
    """
    }

    @Query("""$mainQuery WHERE P.uuid IN (:uuids)""")
    fun searchByIds(uuids: List<UUID>): Single<List<PatientSearchResult>>

    @Query("""$mainQuery
      WHERE (P.uuid IN (:uuids))
      AND ((P.dateOfBirth BETWEEN :dobUpperBound AND :dobLowerBound) OR (P.age_computedDateOfBirth BETWEEN :dobUpperBound AND :dobLowerBound))
      """)
    fun searchByIds(uuids: List<UUID>, dobUpperBound: String, dobLowerBound: String): Single<List<PatientSearchResult>>

    @Query("$mainQuery WHERE P.searchableName LIKE '%' || :name || '%'")
    fun search(name: String): Flowable<List<PatientSearchResult>>

    @Query("""$mainQuery
      WHERE P.searchableName LIKE '%' || :name || '%'
      AND ((P.dateOfBirth BETWEEN :dobUpperBound AND :dobLowerBound) OR (P.age_computedDateOfBirth BETWEEN :dobUpperBound AND :dobLowerBound))
      """)
    fun search(name: String, dobUpperBound: String, dobLowerBound: String): Flowable<List<PatientSearchResult>>

    @Query("$mainQuery WHERE P.syncStatus == :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<PatientSearchResult>>
  }

  fun toPayload(): PatientPayload {
    val payload = PatientPayload(
        uuid = uuid,
        fullName = fullName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        age = age?.value,
        ageUpdatedAt = age?.updatedAt,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        phoneNumbers = null,
        address = address.toPayload())

    if (phoneUuid != null && phoneNumber != null) {
      return payload.copy(
          phoneNumbers = listOf(PatientPhoneNumberPayload(
              uuid = phoneUuid,
              number = phoneNumber,
              type = phoneType!!,
              active = phoneActive!!,
              createdAt = phoneCreatedAt!!,
              updatedAt = phoneUpdatedAt!!
          )))
    }

    return payload
  }

  data class LastBp(
      val takenOn: Instant,
      val takenAtFacilityName: String,
      val takenAtFacilityUuid: UUID
  )
}
