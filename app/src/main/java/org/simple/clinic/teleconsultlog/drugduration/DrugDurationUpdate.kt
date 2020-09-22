package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class DrugDurationUpdate : Update<DrugDurationModel, DrugDurationEvent, DrugDurationEffect> {

  override fun update(model: DrugDurationModel, event: DrugDurationEvent): Next<DrugDurationModel, DrugDurationEffect> {
    return when (event) {
      is DurationChanged -> next(
          model.durationChanged(event.duration)
      )
      DrugDurationSaveClicked -> drugDurationSaveClicked(model)
    }
  }

  private fun drugDurationSaveClicked(model: DrugDurationModel): Next<DrugDurationModel, DrugDurationEffect> {
    return if (model.hasDuration) {
      dispatch(SaveDrugDuration(model.duration.toInt()))
    } else {
      next(model.durationInvalid(Blank))
    }
  }
}
