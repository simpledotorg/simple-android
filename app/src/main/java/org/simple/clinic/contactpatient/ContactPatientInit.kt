package org.simple.clinic.contactpatient

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ContactPatientInit : Init<ContactPatientModel, ContactPatientEffect> {

  override fun init(
      model: ContactPatientModel
  ): First<ContactPatientModel, ContactPatientEffect> {
    val effects = mutableSetOf<ContactPatientEffect>()

    if (!model.hasLoadedPatientProfile) {
      effects.add(LoadContactPatientProfile(model.patientUuid))
    }

    if (!model.hasLoadedOverdueAppointment) {
      effects.add(LoadLatestOverdueAppointment(model.patientUuid))
    }

    if (!model.hasCurrentFacility) {
      effects.add(LoadCurrentFacility)
    }

    return first(model, effects)
  }
}
