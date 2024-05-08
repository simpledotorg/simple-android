package org.simple.clinic.reassignpatient

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ReassignPatientInit : Init<ReassignPatientModel, ReassignPatientEffect> {

  override fun init(model: ReassignPatientModel): First<ReassignPatientModel, ReassignPatientEffect> {
    val effects = mutableSetOf<ReassignPatientEffect>()

    if (!model.hasAssignedFacility) {
      effects.add(LoadAssignedFacility(model.patientUuid))
    }

    return first(model, effects)
  }
}

