package org.simple.clinic.summary.addcholesterol

import org.simple.clinic.summary.addcholesterol.CholesterolEntrySaveState.NOT_SAVING_CHOLESTEROL
import org.simple.clinic.summary.addcholesterol.CholesterolEntrySaveState.SAVING_CHOLESTEROL
import java.util.UUID

data class CholesterolEntryModel(
    val patientUUID: UUID,
    val cholesterolValue: Float,
    val cholesterolSaveState: CholesterolEntrySaveState,
) {

  companion object {
    fun create(patientUUID: UUID): CholesterolEntryModel {
      return CholesterolEntryModel(
          patientUUID = patientUUID,
          cholesterolValue = 0f,
          cholesterolSaveState = NOT_SAVING_CHOLESTEROL,
      )
    }
  }

  fun cholesterolChanged(cholesterolValue: Float): CholesterolEntryModel {
    return copy(cholesterolValue = cholesterolValue)
  }

  fun savingCholesterol(): CholesterolEntryModel {
    return copy(cholesterolSaveState = SAVING_CHOLESTEROL)
  }

  fun cholesterolSaved(): CholesterolEntryModel {
    return copy(cholesterolSaveState = NOT_SAVING_CHOLESTEROL)
  }
}
