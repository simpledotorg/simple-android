package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language
import org.resolvetosavelives.red.sync.PatientAddressPayload
import org.resolvetosavelives.red.sync.PatientPayload
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

data class PatientWithAddress(

    val uuid: String,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    val ageWhenCreated: Int?,

    val status: PatientStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus,

    @Embedded(prefix = "address_")
    val address: PatientAddress
) {

  fun toPayload(): PatientPayload {
    return PatientPayload(
        uuid = uuid,
        fullName = fullName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        ageWhenCreated = ageWhenCreated,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
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
        }
    )
  }

  @Dao
  interface RoomDao {

    companion object {
      @Language("RoomSql")
      const val joinQuery = "SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.ageWhenCreated, P.status, P.createdAt, P.updatedAt, P.syncStatus, " +
          "PA.uuid address_uuid, PA.colonyOrVillage address_colonyOrVillage, PA.district address_district, PA.state address_state, PA.country address_country, PA.createdAt address_createdAt, PA.updatedAt address_updatedAt " +
          "FROM patient P " +
          "INNER JOIN PatientAddress PA on PA.uuid = P.addressUuid"
    }

    @Query("$joinQuery WHERE fullName LIKE '%' || :query || '%'")
    fun search(query: String): Flowable<List<PatientWithAddress>>

    @Query("$joinQuery WHERE P.syncStatus == :status")
    fun withSyncStatus(status: SyncStatus): Flowable<List<PatientWithAddress>>
  }
}
