package org.simple.clinic.teleconsultlog.shareprescription

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultSharePrescriptionUiRendererTest {

  @Test
  fun `when patient details are loaded, then render patient details`() {
    // given
    val ui = mock<TeleconsultSharePrescriptionUi>()
    val uiRenderer = TeleconsultSharePrescriptionUiRenderer(ui = ui)
    val patientUuid = UUID.fromString("507b8273-f8dd-4889-ab12-99b9305b84e4")
    val patient = TestData.patient(patientUuid)
    val model = TeleconsultSharePrescriptionModel
        .create(patientUuid = patientUuid)
        .patientLoaded(patient = patient)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderPatientDetails(patient)
    verifyNoMoreInteractions(ui)
  }

}
