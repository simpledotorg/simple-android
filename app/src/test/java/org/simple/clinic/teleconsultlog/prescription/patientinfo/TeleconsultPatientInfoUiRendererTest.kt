package org.simple.clinic.teleconsultlog.prescription.patientinfo

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class TeleconsultPatientInfoUiRendererTest {

  private val ui = mock<TeleconsultPatientInfoUi>()
  private val uiRenderer = TeleconsultPatientInfoUiRenderer(ui)

  private val patientUuid = UUID.fromString("77f1d870-5c60-49f7-a4e2-2f1d60e4218c")
  private val prescriptionDate = LocalDate.parse("2018-01-01")
  private val model = TeleconsultPatientInfoModel.create(
      patientUuid = patientUuid,
      prescriptionDate = prescriptionDate
  )

  @Test
  fun `when patient details are loaded, then render patient information`() {
    // given
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid
    )
    val patientLoadedModel = model.patientProfileLoaded(patientProfile)

    // when
    uiRenderer.render(patientLoadedModel)

    // then
    verify(ui).renderPatientInformation(patientProfile)
    verify(ui).renderPrescriptionDate(prescriptionDate)
    verifyNoMoreInteractions(ui)
  }
}
