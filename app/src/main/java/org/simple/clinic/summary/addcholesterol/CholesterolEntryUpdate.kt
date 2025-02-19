package org.simple.clinic.summary.addcholesterol

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class CholesterolEntryUpdate(
    private val minReqCholesterol: Float = 25f,
    private val maxReqCholesterol: Float = 1000f,
) : Update<CholesterolEntryModel, CholesterolEntryEvent, CholesterolEntryEffect> {

  override fun update(model: CholesterolEntryModel, event: CholesterolEntryEvent): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    return when (event) {
      is CholesterolChanged -> cholesterolChanged(model, event)
      SaveClicked -> onSaveClicked(model)
      CholesterolSaved -> onCholesterolSaved(model)
      KeyboardClosed -> onKeyboardClosed()
    }
  }

  private fun onKeyboardClosed(): Next<CholesterolEntryModel, CholesterolEntryEffect> =
      dispatch(DismissSheet)

  private fun onCholesterolSaved(model: CholesterolEntryModel): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    return next(model.cholesterolSaved(), DismissSheet)
  }

  private fun onSaveClicked(model: CholesterolEntryModel): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    return when {
      model.cholesterolValue < minReqCholesterol -> {
        dispatch(ShowReqMinCholesterolValidationError)
      }

      model.cholesterolValue > maxReqCholesterol -> {
        dispatch(ShowReqMaxCholesterolValidationError)
      }

      else -> {
        next(
            model.savingCholesterol(),
            SaveCholesterol(
                patientUuid = model.patientUUID,
                cholesterolValue = model.cholesterolValue
            )
        )
      }
    }
  }

  private fun cholesterolChanged(model: CholesterolEntryModel, event: CholesterolChanged): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    return next(
        model.cholesterolChanged(event.cholesterolValue),
        HideCholesterolErrorMessage
    )
  }
}
