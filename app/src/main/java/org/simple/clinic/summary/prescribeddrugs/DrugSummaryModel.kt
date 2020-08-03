package org.simple.clinic.summary.prescribeddrugs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.drugs.PrescribedDrug
import java.util.UUID

@Parcelize
data class DrugSummaryModel(
    val patientUuid: UUID,
    val prescribedDrugs: List<PrescribedDrug>?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID) = DrugSummaryModel(
        patientUuid = patientUuid,
        prescribedDrugs = null
    )
  }

  fun prescribedDrugsLoaded(prescribedDrugs: List<PrescribedDrug>): DrugSummaryModel {
    return copy(prescribedDrugs = prescribedDrugs)
  }
}
