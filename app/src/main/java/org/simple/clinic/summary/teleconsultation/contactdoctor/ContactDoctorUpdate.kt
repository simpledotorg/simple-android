package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class ContactDoctorUpdate : Update<ContactDoctorModel, ContactDoctorEvent, ContactDoctorEffect> {

  override fun update(model: ContactDoctorModel, event: ContactDoctorEvent): Next<ContactDoctorModel, ContactDoctorEffect> {
    return when (event) {
      is MedicalOfficersLoaded -> next(model.medicalOfficersLoaded(event.medicalOfficers))
      is TeleconsultRequestCreated -> dispatch(LoadPatientTeleconsultInfo(
          patientUuid = model.patientUuid,
          teleconsultRecordId = event.teleconsultRecordId,
          doctorPhoneNumber = event.doctorPhoneNumber,
          messageTarget = event.messageTarget
      ))
      is PatientTeleconsultInfoLoaded -> dispatch(SendTeleconsultMessage(
          teleconsultInfo = event.patientTeleconsultInfo,
          messageTarget = event.messageTarget
      ))
      is WhatsAppButtonClicked -> dispatch(CreateTeleconsultRequest(
          patientUuid = model.patientUuid,
          medicalOfficerId = event.medicalOfficerId,
          doctorPhoneNumber = event.doctorPhoneNumber,
          messageTarget = MessageTarget.WHATSAPP
      ))
      is SmsButtonClicked -> dispatch(CreateTeleconsultRequest(
          patientUuid = model.patientUuid,
          medicalOfficerId = event.medicalOfficerId,
          doctorPhoneNumber = event.doctorPhoneNumber,
          messageTarget = MessageTarget.SMS
      ))
    }
  }
}
