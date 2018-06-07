package org.resolvetosavelives.red.patient

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Query
import io.reactivex.Flowable
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

    val phoneNumber: String?,

    val phoneActive: Boolean?
) {

  @Dao
  interface RoomDao {

    companion object {
      @Language("RoomSql")
      const val mainQuery = """
          SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.status, P.createdAt, P.updatedAt, P.syncStatus,
          PA.uuid addr_uuid, PA.colonyOrVillage addr_colonyOrVillage, PA.district addr_district, PA.state addr_state, PA.country addr_country,
          PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt,
          PP.number phoneNumber, PP.active phoneActive
          FROM Patient P
          INNER JOIN PatientAddress PA on PA.uuid = P.addressUuid
          INNER JOIN PatientPhoneNumber PP ON P.uuid = PP.patientUuid
    """
    }

    @Query("$mainQuery WHERE P.fullname LIKE '%' || :query || '%' OR PP.number LIKE '%' || :query || '%'")
    fun search(query: String): Flowable<List<PatientSearchResult>>

    @Query(mainQuery)
    fun allRecords(): Flowable<List<PatientSearchResult>>
  }
}
