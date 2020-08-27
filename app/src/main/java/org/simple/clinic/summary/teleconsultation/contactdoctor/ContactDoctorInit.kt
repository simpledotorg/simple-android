package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ContactDoctorInit : Init<ContactDoctorModel, ContactDoctorEffect> {

  override fun init(model: ContactDoctorModel): First<ContactDoctorModel, ContactDoctorEffect> {
    val effects = mutableSetOf<ContactDoctorEffect>()
    if (model.hasMedicalOfficers.not()) {
      effects.add(LoadMedicalOfficers)
    }

    return first(model, effects)
  }
}
