package org.simple.clinic.search

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class PatientSearchInit : Init<PatientSearchModel, PatientSearchEffect> {

  override fun init(model: PatientSearchModel): First<PatientSearchModel, PatientSearchEffect> {
    return first(model)
  }
}
