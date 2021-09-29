package org.simple.clinic.teleconsultlog.prescription.medicines

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.PrescribedDrug
import java.util.UUID

@Parcelize
data class TeleconsultMedicinesModel(
    val patientUuid: UUID,
    val medicines: List<PrescribedDrug>?
) : Parcelable {

  companion object {

    fun create(patientUuid: UUID) = TeleconsultMedicinesModel(
        patientUuid = patientUuid,
        medicines = null
    )
  }

  val hasMedicines: Boolean
    get() = medicines != null

  fun medicinesLoaded(medicines: List<PrescribedDrug>): TeleconsultMedicinesModel {
    return copy(medicines = medicines)
  }
}
