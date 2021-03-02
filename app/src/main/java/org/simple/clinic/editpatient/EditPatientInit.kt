package org.simple.clinic.editpatient

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId

class EditPatientInit(
    private val patient: Patient,
    private val address: PatientAddress,
    private val phoneNumber: PatientPhoneNumber?,
    private val bangladeshNationalId: BusinessId?
) : Init<EditPatientModel, EditPatientEffect> {
  override fun init(model: EditPatientModel): First<EditPatientModel, EditPatientEffect> {
    val effects = mutableSetOf(
        PrefillFormEffect(patient, address, phoneNumber, bangladeshNationalId),
        FetchBpPassportsEffect(patient.uuid),
        LoadInputFields,
    )

    if (!model.hasColonyOrVillagesList) {
      effects.add(FetchColonyOrVillagesEffect)
    }

    return first(model, effects)
  }
}
