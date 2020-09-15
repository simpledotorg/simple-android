package org.simple.clinic.teleconsultlog.prescription

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultPrescriptionUiRendererTest {

  @Test
  fun `when patient details are loaded, then render patient details`() {
    // given
    val ui = mock<TeleconsultPrescriptionUi>()
    val uiRenderer = TeleconsultPrescriptionUiRenderer(ui)
    val patientUuid = UUID.fromString("76066faf-b1a2-4a15-8134-31c722ad11c5")
    val patient = TestData.patient(uuid = patientUuid)
    val model = TeleconsultPrescriptionModel
        .create(patientUuid = patientUuid)
        .patientLoaded(patient = patient)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderPatientDetails(patient)
    verifyNoMoreInteractions(ui)
  }
}
