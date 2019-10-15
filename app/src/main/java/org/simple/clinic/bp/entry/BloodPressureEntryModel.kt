package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.OpenAs.New
import java.util.UUID

data class BloodPressureEntryModel(
    val openAs: OpenAs
) {
  companion object {
    fun newBloodPressureEntry(patientUuid: UUID): BloodPressureEntryModel =
        BloodPressureEntryModel(New(patientUuid))
  }
}
