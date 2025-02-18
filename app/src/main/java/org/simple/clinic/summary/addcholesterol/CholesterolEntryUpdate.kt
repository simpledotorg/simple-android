package org.simple.clinic.summary.addcholesterol

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class CholesterolEntryUpdate(
    private val minReqCholesterol: Float = 25f,
    private val maxReqCholesterol: Float = 1000f,
) : Update<CholesterolEntryModel, CholesterolEntryEvent, CholesterolEntryEffect> {

  override fun update(model: CholesterolEntryModel, event: CholesterolEntryEvent): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    return when (event) {
      is CholesterolChanged -> cholesterolChanged(model, event)
      SaveClicked -> onSaveClicked(model)
    }
  }

  private fun onSaveClicked(model: CholesterolEntryModel): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    val effects = mutableSetOf<CholesterolEntryEffect>()

    when {
      model.cholesterolValue < minReqCholesterol -> {
        effects.add(ShowReqMinCholesterolValidationError)
      }

      model.cholesterolValue > maxReqCholesterol -> {
        effects.add(ShowReqMaxCholesterolValidationError)
      }
    }

    return dispatch(effects)
  }

  private fun cholesterolChanged(model: CholesterolEntryModel, event: CholesterolChanged): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    return next(
        model.cholesterolChanged(event.cholesterolValue),
        HideCholesterolErrorMessage
    )
  }
}
