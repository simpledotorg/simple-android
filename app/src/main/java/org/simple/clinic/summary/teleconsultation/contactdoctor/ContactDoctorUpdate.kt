package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class ContactDoctorUpdate : Update<ContactDoctorModel, ContactDoctorEvent, ContactDoctorEffect> {

  override fun update(model: ContactDoctorModel, event: ContactDoctorEvent): Next<ContactDoctorModel, ContactDoctorEffect> {
    return when (event) {
      is MedicalOfficersLoaded -> next(model.medicalOfficersLoaded(event.medicalOfficers))
      is TeleconsultRequestCreated -> noChange()
    }
  }
}
