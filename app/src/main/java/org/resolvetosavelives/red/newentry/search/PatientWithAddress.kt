package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Query
import io.reactivex.Flowable
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

    val syncPending: Boolean,

    @Embedded(prefix = "address_")
    val address: PatientAddress
) {

  @Dao
  interface RoomDao {

    @Query("SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.ageWhenCreated, P.status, P.createdAt, P.updatedAt, P.syncPending, " +
        "PA.uuid address_uuid, PA.colonyOrVillage address_colonyOrVillage, PA.district address_district, PA.state address_state, PA.country address_country, PA.createdAt address_createdAt, PA.updatedAt address_updatedAt, PA.syncPending address_syncPending   " +
        "FROM patient P " +
        "INNER JOIN PatientAddress PA on PA.uuid = P.addressUuid " +
        "WHERE fullName LIKE '%' || :query || '%'")
    fun search(query: String): Flowable<List<PatientWithAddress>>
  }
}
