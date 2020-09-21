package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class TeleconsultDoctorInfoUpdate : Update<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEvent, TeleconsultDoctorInfoEffect> {

  override fun update(
      model: TeleconsultDoctorInfoModel,
      event: TeleconsultDoctorInfoEvent
  ): Next<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {
    return when (event) {
      is MedicalRegistrationIdLoaded -> medicalRegistrationIdLoaded(model, event)
      is SignatureBitmapLoaded -> signatureLoaded(event)
      is MedicalRegistrationIdChanged -> next(model.medicalRegistrationIdLoaded(event.medicalRegistrationId))
      is MedicalInstructionsChanged -> next(model.medicalInstructionsChanged(event.instructions))
      is CurrentUserLoaded -> noChange()
    }
  }

  private fun signatureLoaded(event: SignatureBitmapLoaded): Next<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {
    return if (event.signatureBitmap != null) {
      dispatch(SetSignatureBitmap(event.signatureBitmap))
    } else {
      noChange()
    }
  }

  private fun medicalRegistrationIdLoaded(
      model: TeleconsultDoctorInfoModel,
      event: MedicalRegistrationIdLoaded
  ): Next<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {
    return next(model.medicalRegistrationIdLoaded(event.medicalRegistrationId), SetMedicalRegistrationId(event.medicalRegistrationId))
  }
}
