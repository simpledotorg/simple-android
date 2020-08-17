package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class DrugDurationUpdate : Update<DrugDurationModel, DrugDurationEvent, DrugDurationEffect> {

  override fun update(model: DrugDurationModel, event: DrugDurationEvent): Next<DrugDurationModel, DrugDurationEffect> {
    return when (event) {
      DurationChanged -> dispatch(HideDurationError)
      is DrugDurationSaveClicked -> drugDurationSaveClicked(event)
    }
  }

  private fun drugDurationSaveClicked(event: DrugDurationSaveClicked): Next<DrugDurationModel, DrugDurationEffect> {
    return if (event.duration.isNotBlank()) {
      dispatch(SaveDrugDuration(event.duration.toInt()))
    } else {
      dispatch(ShowBlankDurationError)
    }
  }
}
