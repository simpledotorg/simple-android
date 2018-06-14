package org.resolvetosavelives.red.patient

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language
import org.resolvetosavelives.red.patient.sync.PatientAddressPayload
import org.resolvetosavelives.red.patient.sync.PatientPayload
import org.resolvetosavelives.red.patient.sync.PatientPhoneNumberPayload
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

    val phoneUuid: UUID?,

    val phoneNumber: String?,

    val phoneType: PatientPhoneNumberType?,

    val phoneActive: Boolean?,

    val phoneCreatedAt: Instant?,

    val phoneUpdatedAt: Instant?
) {

  @Dao
  interface RoomDao {

    companion object {
      @Language("RoomSql")
      const val mainQuery = """
          SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.age_computedDateOfBirth, P.status, P.createdAt, P.updatedAt, P.syncStatus,
          PA.uuid addr_uuid, PA.colonyOrVillage addr_colonyOrVillage, PA.district addr_district, PA.state addr_state, PA.country addr_country,
          PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt,
          PP.uuid phoneUuid, PP.number phoneNumber, PP.phoneType phoneType, PP.active phoneActive, PP.createdAt phoneCreatedAt, PP.updatedAt phoneUpdatedAt
          FROM Patient P
          INNER JOIN PatientAddress PA on PA.uuid = P.addressUuid
          LEFT JOIN PatientPhoneNumber PP ON PP.patientUuid = P.uuid
    """
    }

    @Query("$mainQuery WHERE P.fullname LIKE '%' || :query || '%' OR PP.number LIKE '%' || :query || '%'")
    fun search(query: String): Flowable<List<PatientSearchResult>>

    @Query("""$mainQuery
      WHERE (P.fullname LIKE '%' || :query || '%' OR PP.number LIKE '%' || :query || '%')
      AND ((P.dateOfBirth BETWEEN :dobUpperBound AND :dobLowerBound) OR (P.age_computedDateOfBirth BETWEEN :dobUpperBound AND :dobLowerBound))
      """)
    fun search(query: String, dobUpperBound: String, dobLowerBound: String): Flowable<List<PatientSearchResult>>

    @Query("$mainQuery ORDER BY P.updatedAt DESC LIMIT 100")
    fun recentlyUpdated100Records(): Flowable<List<PatientSearchResult>>

    @Transaction
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
        address = with(address) {
          PatientAddressPayload(
              uuid = uuid,
              colonyOrVillage = colonyOrVillage,
              district = district,
              state = state,
              country = country,
              createdAt = createdAt,
              updatedAt = updatedAt
          )
        })

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

}
