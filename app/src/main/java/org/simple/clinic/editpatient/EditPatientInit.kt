package org.simple.clinic.editpatient

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber

class EditPatientInit(
    private val patient: Patient,
    private val address: PatientAddress,
    private val phoneNumber: PatientPhoneNumber?
) : Init<EditPatientModel, EditPatientEffect> {
  override fun init(model: EditPatientModel): First<EditPatientModel, EditPatientEffect> {
    return first(model, setOf(PrefillFormEffect(patient, address, phoneNumber)))
  }
}
