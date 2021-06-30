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
      effects.add(LoadPatientProfile(model.patientUuid))
    }

    if (!model.hasLoadedAppointment && !model.overdueListChangesFeatureEnabled) {
      effects.add(LoadLatestOverdueAppointment_Old(model.patientUuid))
    }

    if (!model.hasLoadedAppointment && model.overdueListChangesFeatureEnabled) {
      effects.add(LoadLatestOverdueAppointment(model.patientUuid))
    }

    val updatedModel = if (model.hasLoadedAppointment && model.hasLoadedPatientProfile) {
      model.contactPatientInfoLoaded()
    } else {
      model.contactPatientInfoLoading()
    }

    return first(updatedModel, effects)
  }
}
