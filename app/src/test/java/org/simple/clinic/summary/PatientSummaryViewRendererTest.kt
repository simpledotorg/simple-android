package org.simple.clinic.summary

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class PatientSummaryViewRendererTest {

  private val facilityWithDiabetesManagementEnabled = PatientMocker.facility(
      uuid = UUID.fromString("9eb182ee-1ec8-4d19-89b8-abe66ed993d9"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
  )

  private val facilityWithDiabetesManagementDisabled = PatientMocker.facility(
      uuid = UUID.fromString("9eb182ee-1ec8-4d19-89b8-abe66ed993d9"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
  )

  private val defaultModel = PatientSummaryModel.from(UUID.fromString("6fdf088e-f6aa-40e9-9cc2-22e197b83470"))
  private val ui = mock<PatientSummaryScreenUi>()

  private val uiRenderer = PatientSummaryViewRenderer(ui)

  @Test
  fun `when the facility supports diabetes management, the diabetes widget must be shown`() {
    // given
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the facility does not support diabetes management, the diabetes widget must be hidden`() {
    // given
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementDisabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideDiabetesView()
    verifyNoMoreInteractions(ui)
  }
}
