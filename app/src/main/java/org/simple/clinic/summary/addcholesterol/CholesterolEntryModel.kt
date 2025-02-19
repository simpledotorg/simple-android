package org.simple.clinic.summary.addcholesterol

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.summary.addcholesterol.CholesterolEntrySaveState.NOT_SAVING_CHOLESTEROL
import org.simple.clinic.summary.addcholesterol.CholesterolEntrySaveState.SAVING_CHOLESTEROL
import java.util.UUID

@Parcelize
data class CholesterolEntryModel(
    val patientUUID: UUID,
    val cholesterolValue: Float,
    val cholesterolSaveState: CholesterolEntrySaveState,
) : Parcelable {

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
