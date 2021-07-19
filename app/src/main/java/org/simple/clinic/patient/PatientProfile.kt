package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import java.util.UUID

@Parcelize
data class PatientProfile(
    @Embedded
    val patient: Patient,

    @Relation(
        parentColumn = "addressUuid",
        entityColumn = "uuid"
    )
    val address: PatientAddress,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "patientUuid"
    )
    val phoneNumbers: List<PatientPhoneNumber>,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "patientUuid"
    )
    val businessIds: List<BusinessId>
) : Parcelable {
  val patientUuid: UUID
    get() = patient.uuid

  val hasNationalHealthID: Boolean
    get() = businessIds
        .any {
          it.identifier.type == IndiaNationalHealthId
        }

  fun withoutDeletedBusinessIds(): PatientProfile {
    return copy(businessIds = businessIds.filter { it.deletedAt == null })
  }

  fun withoutDeletedPhoneNumbers(): PatientProfile {
    return copy(phoneNumbers = phoneNumbers.filter { it.deletedAt == null })
  }
}
