package org.simple.clinic.editpatient

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.DateOfBirth.Type.EXACT
import org.simple.clinic.patient.DateOfBirth.Type.FROM_AGE
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.util.UserClock

object EditPatientEffectHandler {
  fun createEffectHandler(ui: EditPatientUi, userClock: UserClock): ObservableTransformer<EditPatientEffect, EditPatientEvent> {
    return RxMobius
        .subtypeEffectHandler<EditPatientEffect, EditPatientEvent>()
        .addConsumer(PrefillFormEffect::class.java) { (patient, address, phoneNumber) ->
          prefillFormFields(ui, patient, address, phoneNumber, userClock)
        }
        .build()
  }

  private fun prefillFormFields(
      ui: EditPatientUi,
      patient: Patient,
      address: PatientAddress,
      phoneNumber: PatientPhoneNumber?,
      userClock: UserClock
  ) {
    ui.setPatientName(patient.fullName)
    ui.setGender(patient.gender)
    phoneNumber?.let { ui.setPatientPhoneNumber(it.number) }
    ui.setState(address.state)
    ui.setDistrict(address.district)

    if (address.colonyOrVillage.isNullOrBlank().not()) {
      ui.setColonyOrVillage(address.colonyOrVillage!!)
    }

    val dateOfBirth = DateOfBirth.fromPatient(patient, userClock)
    when (dateOfBirth.type) {
      EXACT -> ui.setPatientDateOfBirth(dateOfBirth.date)
      FROM_AGE -> ui.setPatientAge(dateOfBirth.estimateAge(userClock))
    }
  }
}
