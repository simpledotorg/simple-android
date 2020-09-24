package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class DrugDurationInit : Init<DrugDurationModel, DrugDurationEffect> {

  override fun init(model: DrugDurationModel): First<DrugDurationModel, DrugDurationEffect> {
    return first(model, PrefillDrugDuration(model.duration))
  }
}
