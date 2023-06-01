package org.simple.clinic.teleconsultlog.prescription

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class TeleconsultPrescriptionUiRendererTest {

  @Test
  fun `when patient details are loaded, then render patient details`() {
    // given
    val ui = mock<TeleconsultPrescriptionUi>()
    val uiRenderer = TeleconsultPrescriptionUiRenderer(ui)
    val teleconsultRecordId = UUID.fromString("c3e5dcd9-6443-4c0d-9d70-cac0a9221ebf")
    val patientUuid = UUID.fromString("76066faf-b1a2-4a15-8134-31c722ad11c5")
    val patient = TestData.patient(uuid = patientUuid)
    val model = TeleconsultPrescriptionModel
        .create(teleconsultRecordId = teleconsultRecordId, patientUuid = patientUuid)
        .patientLoaded(patient = patient)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderPatientDetails(patient)
    verifyNoMoreInteractions(ui)
  }
}
