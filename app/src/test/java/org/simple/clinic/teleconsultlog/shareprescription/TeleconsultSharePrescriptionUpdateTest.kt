package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
import com.nhaarman.mockitokotlin2.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class TeleconsultSharePrescriptionUpdateTest {

  private val updateSpec = UpdateSpec(TeleconsultSharePrescriptionUpdate())
  private val patientUuid: UUID = UUID.fromString("b0d1047f-4d76-4518-b6d9-daa5c4bb1c7e")
  private val prescriptionDate = LocalDate.parse("2020-10-01")
  private val model = TeleconsultSharePrescriptionModel
      .create(patientUuid = patientUuid, prescriptionDate = prescriptionDate)

  @Test
  fun `when patient details are loaded, update the model`() {
    val patient = TestData.patient(patientUuid)
    updateSpec
        .given(model)
        .whenEvents(PatientDetailsLoaded(patient = patient))
        .then(
            assertThatNext(
                hasModel(model.patientLoaded(patient)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when patient medicines are loaded, then update the model`() {
    val prescriptionUuid1 = UUID.fromString("76beea9b-74a1-4c10-b9bf-2b0ac7ccce4f")
    val prescriptionUuid2 = UUID.fromString("12b2cfb1-9af2-440c-90fe-ba475f934138")
    val medicines = listOf(
        TestData.prescription(
            uuid = prescriptionUuid1,
            patientUuid = patientUuid
        ),
        TestData.prescription(
            uuid = prescriptionUuid2,
            patientUuid = patientUuid
        )
    )
    updateSpec
        .given(model)
        .whenEvents(PatientMedicinesLoaded(medicines = medicines))
        .then(
            assertThatNext(
                hasModel(model.patientMedicinesLoaded(medicines = medicines)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when medical officer's signature is loaded, then set the signature bitmap`() {
    val bitmap = mock<Bitmap>()
    updateSpec
        .given(model)
        .whenEvents(SignatureLoaded(bitmap))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(SetSignature(bitmap))
            )
        )
  }

  @Test
  fun `when medical registration exists and is loaded, then set the medical registration Id`(){
    val medicalRegistrationId = "1111111111"

    updateSpec
        .given(model)
        .whenEvents(MedicalRegistrationIdLoaded(medicalRegistrationId))
        .then(
            assertThatNext(
                hasModel(model.medicalRegistrationIdLoaded(medicalRegistrationId)),
                hasEffects(SetMedicalRegistrationId(medicalRegistrationId))
            )
        )
  }

  @Test
  fun `when done button is clicked, go to home screen`() {
    updateSpec
        .given(model)
        .whenEvents(DoneClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(GoToHomeScreen)
            )
        )
  }

  @Test
  fun `when patient profile are loaded, then update the model`() {
    val patientProfile = TestData.patientProfile(patientUuid = patientUuid)

    updateSpec
        .given(model)
        .whenEvent(PatientProfileLoaded(patientProfile))
        .then(assertThatNext(
            hasModel(model.patientProfileLoaded(patientProfile)),
            hasNoEffects()
        ))
  }
}
