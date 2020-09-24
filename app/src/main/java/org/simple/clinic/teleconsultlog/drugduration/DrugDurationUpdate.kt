package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import javax.inject.Inject

class DrugDurationUpdate @Inject constructor(
    private val validator: DrugDurationValidator
) : Update<DrugDurationModel, DrugDurationEvent, DrugDurationEffect> {

  override fun update(model: DrugDurationModel, event: DrugDurationEvent): Next<DrugDurationModel, DrugDurationEffect> {
    return when (event) {
      is DurationChanged -> next(
          model.durationChanged(event.duration)
      )
      DrugDurationSaveClicked -> drugDurationSaveClicked(model)
    }
  }

  private fun drugDurationSaveClicked(model: DrugDurationModel): Next<DrugDurationModel, DrugDurationEffect> {
    return when (val result = validator.validate(model.duration)) {
      Blank -> next(model.durationInvalid(result))
      is MaxDrugDuration -> next(model.durationInvalid(result))
      Valid -> dispatch(SaveDrugDuration(model.duration.toInt()))
    }
  }
}
