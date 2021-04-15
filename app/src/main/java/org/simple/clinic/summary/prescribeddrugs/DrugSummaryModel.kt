package org.simple.clinic.summary.prescribeddrugs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.summary.PatientSummaryChildModel
import java.util.UUID

@Parcelize
data class DrugSummaryModel(
    val patientUuid: UUID,
    val prescribedDrugs: List<PrescribedDrug>?
) : Parcelable, PatientSummaryChildModel {

  companion object {
    fun create(patientUuid: UUID) = DrugSummaryModel(
        patientUuid = patientUuid,
        prescribedDrugs = null
    )
  }

  val hasPrescribedDrugs: Boolean
    get() = prescribedDrugs != null

  val prescribedDrugsNotNullorEmpty: Boolean
    get() = !prescribedDrugs.isNullOrEmpty()

  override fun readyToRender(): Boolean {
    return hasPrescribedDrugs
  }

  fun prescribedDrugsLoaded(prescribedDrugs: List<PrescribedDrug>): DrugSummaryModel {
    return copy(prescribedDrugs = prescribedDrugs)
  }
}
