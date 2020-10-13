package org.simple.clinic.teleconsultlog.shareprescription

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class TeleconsultSharePrescriptionUiRendererTest {

  private val ui = mock<TeleconsultSharePrescriptionUi>()
  private val uiRenderer = TeleconsultSharePrescriptionUiRenderer(ui = ui)
  private val patientUuid = UUID.fromString("507b8273-f8dd-4889-ab12-99b9305b84e4")
  private val prescriptionDate = LocalDate.parse("2020-10-01")

  val model = TeleconsultSharePrescriptionModel
      .create(patientUuid = patientUuid, prescriptionDate = prescriptionDate)


  @Test
  fun `when patient profile details are loaded, then render the patient profile`() {
    // given
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid
    )

    val model = model
        .patientProfileLoaded(patientProfile)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderPatientInformation(patientProfile)
    verify(ui).renderPrescriptionDate(prescriptionDate = prescriptionDate)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when medicines are loaded, then render the medicines`() {
    // given
    val medicines = listOf(
        TestData.prescription(
            uuid = UUID.fromString("3b3bc1c4-7e27-4e9f-9ce8-7464a6c8d129")
        ),
        TestData.prescription(
            uuid = UUID.fromString("3b3bc1c4-7e27-4e9f-9ce8-7464a6c8d129")
        )
    )

    val model = model
        .patientMedicinesLoaded(medicines)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderPatientMedicines(medicines)
    verifyNoMoreInteractions(ui)
  }
}
