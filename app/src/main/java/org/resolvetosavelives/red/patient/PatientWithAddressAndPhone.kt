package org.resolvetosavelives.red.patient

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@Deprecated(message = "You should use PatientSearchResult instead.")
data class PatientWithAddressAndPhone(

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

    @Embedded(prefix = "address_")
    val address: PatientAddress
) {

  @Relation(parentColumn = "uuid", entityColumn = "patientUuid")
  var phoneNumbers: List<PatientPhoneNumber>? = null


//  @Dao
//  interface RoomDao {
//
//    companion object {
//      @Language("RoomSql")
//      const val joinQuery = """
//          SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.status, P.createdAt, P.updatedAt, P.syncStatus,
//          PA.uuid address_uuid, PA.colonyOrVillage address_colonyOrVillage, PA.district address_district, PA.state address_state,
//          PA.country address_country, PA.createdAt address_createdAt, PA.updatedAt address_updatedAt
//          FROM patient P
//          INNER JOIN PatientAddress PA on PA.uuid = P.addressUuid
//          """
//    }
//
//    @Transaction
//    @Query("$joinQuery WHERE P.syncStatus == :status")
//    fun syncStatusFilter(status: SyncStatus): Flowable<List<PatientWithAddressAndPhone>>
//
//  }

}
