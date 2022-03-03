package org.simple.clinic.patient

import android.os.Parcelable
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Query
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Unicode
import java.util.UUID

@DatabaseView("""
  SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.assignedFacilityId, P.status,
  PA.uuid addr_uuid, PA.streetAddress addr_streetAddress, PA.colonyOrVillage addr_colonyOrVillage, PA.zone addr_zone, PA.district addr_district,
  PA.state addr_state, PA.country addr_country,
  PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt, PA.deletedAt addr_deletedAt,
  PP.number phoneNumber,
  B.identifier id_identifier, B.identifierType id_identifierType, B.searchHelp identifierSearchHelp, AF.name assignedFacilityName
  FROM Patient P
  INNER JOIN PatientAddress PA ON PA.uuid = P.addressUuid
  LEFT JOIN PatientPhoneNumber PP ON PP.patientUuid = P.uuid
  LEFT JOIN Facility AF ON AF.uuid = P.assignedFacilityId
  LEFT JOIN BusinessId B ON B.patientUuid = P.uuid
  WHERE P.deletedAt IS NULL
""")
@Parcelize
data class PatientSearchResult(

    val uuid: UUID,

    val fullName: String,

    val gender: Gender,

    @Embedded
    val ageDetails: PatientAgeDetails,

    val status: PatientStatus,

    val assignedFacilityId: UUID?,

    val assignedFacilityName: String?,

    @Embedded(prefix = "addr_")
    val address: PatientAddress,

    val phoneNumber: String?,

    @Embedded(prefix = "id_")
    val identifier: Identifier?,

    val identifierSearchHelp: String?
) : Parcelable {

  override fun toString(): String {
    return "PatientSearchResult(${Unicode.redacted})"
  }

  @Dao
  interface RoomDao {

    @Query("""
      SELECT * FROM PatientSearchResult
      WHERE status = :status AND assignedFacilityId = :facilityId
      GROUP BY uuid
      ORDER BY fullName COLLATE NOCASE
    """)
    fun allPatientsInFacility(facilityId: UUID, status: PatientStatus): PagingSource<Int, PatientSearchResult>

    @Query("""
      SELECT * FROM (
        SELECT searchResult.* FROM PatientSearchResult searchResult
        JOIN PatientFts patientFts ON patientFts.uuid = searchResult.uuid
        WHERE patientFts.fullName MATCH :query
        
        UNION
        
        SELECT searchResult.* FROM PatientSearchResult searchResult
        JOIN PatientPhoneNumberFts phoneNumberFts ON phoneNumberFts.patientUuid = searchResult.uuid
        WHERE number MATCH :query
        
        UNION
        
        SELECT searchResult.* FROM PatientSearchResult searchResult
        JOIN BusinessIdFts businessIdFts ON businessIdFts.patientUuid = searchResult.uuid
        WHERE searchHelp MATCH :query
      )
      GROUP BY uuid
      ORDER BY 
      CASE assignedFacilityId
        WHEN :facilityId THEN 0
        ELSE 1 
      END
    """)
    fun search(query: String, facilityId: UUID): PagingSource<Int, PatientSearchResult>
  }
}
