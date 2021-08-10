package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.Drug
import java.util.UUID

sealed class OpenAs {
  sealed class New : OpenAs() {
    data class FromDrugName(val patientUuid: UUID, val drugName: String) : New()

    data class FromDrugList(val patientUuid: UUID, val drug: Drug) : New()
  }

  data class Update(val patientUuid: UUID, val prescribedDrugUuid: UUID) : OpenAs()
}
