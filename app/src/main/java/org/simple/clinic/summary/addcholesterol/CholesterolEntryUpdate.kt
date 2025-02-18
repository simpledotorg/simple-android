package org.simple.clinic.summary.addcholesterol

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class CholesterolEntryUpdate : Update<CholesterolEntryModel, CholesterolEntryEvent, CholesterolEntryEffect> {

  override fun update(model: CholesterolEntryModel, event: CholesterolEntryEvent): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    return when (event) {
      is CholesterolChanged -> cholesterolChanged(model, event)
    }
  }

  private fun cholesterolChanged(model: CholesterolEntryModel, event: CholesterolChanged): Next<CholesterolEntryModel, CholesterolEntryEffect> {
    return next(
        model.cholesterolChanged(event.cholesterolValue),
        HideCholesterolErrorMessage
    )
  }
}
