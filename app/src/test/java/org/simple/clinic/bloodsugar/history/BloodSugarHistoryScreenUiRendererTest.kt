package org.simple.clinic.bloodsugar.history

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class BloodSugarHistoryScreenUiRendererTest {
  private val ui = mock<BloodSugarHistoryScreenUi>()
  private val renderer = BloodSugarHistoryScreenUiRenderer(ui)
  private val patientUuid = UUID.fromString("74bc8b07-1d47-4595-9fe8-f1c7c83a1f2a")
  private val defaultModel = BloodSugarHistoryScreenModel.create(patientUuid)

  @Test
  fun `when blood sugar history is being loaded then do nothing`() {
    // when
    renderer.render(defaultModel)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when patient is loaded, then show it on the ui`() {
    // given
    val patient = PatientMocker.patient(patientUuid)

    // when
    renderer.render(defaultModel.patientLoaded(patient))

    // then
    verify(ui).showPatientInformation(patient)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when blood sugar history is loaded, then show it on the ui`() {
    // given
    val bloodSugar = PatientMocker.bloodSugarMeasurement(
        uuid = UUID.fromString("bfeed199-ca34-4503-aac9-8903f9501c5b"),
        patientUuid = patientUuid
    )
    val bloodSugars = listOf(bloodSugar)

    // when
    renderer.render(defaultModel.bloodSugarsLoaded(bloodSugars))

    // then
    verify(ui).showBloodSugarHistory(bloodSugars)
    verifyNoMoreInteractions(ui)
  }
}
