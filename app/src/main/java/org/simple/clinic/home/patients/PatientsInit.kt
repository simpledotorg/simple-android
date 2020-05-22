package org.simple.clinic.home.patients

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PatientsInit: Init<PatientsModel, PatientsEffect> {

  override fun init(model: PatientsModel): First<PatientsModel, PatientsEffect> {
    return first(model)
  }
}
