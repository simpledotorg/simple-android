package org.simple.clinic.bp.history

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class BloodPressureHistoryScreenUiRendererTest {

  private val ui = mock<BloodPressureHistoryScreenUi>()
  private val renderer = BloodPressureHistoryScreenUiRenderer(ui)
  private val patientUuid = UUID.fromString("9dd563b5-99a5-4f43-b3ab-47c43ed5d62c")
  private val defaultModel = BloodPressureHistoryScreenModel.create(patientUuid)

  @Test
  fun `when blood pressure history is being loaded then do nothing`() {
    //when
    renderer.render(defaultModel)

    //then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when patient is loaded, then show it on the ui`() {
    //given
    val patient = TestData.patient(
        uuid = UUID.fromString("c80bce99-82bc-4d12-a85a-dcae373fece3")
    )

    //when
    renderer.render(defaultModel.patientLoaded(patient))

    //then
    verify(ui).showPatientInformation(patient)
    verifyNoMoreInteractions(ui)
  }
}
