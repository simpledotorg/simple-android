package org.simple.clinic.bp.entry

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.bp.entry.BloodPressureSaveState.SAVING_BLOOD_PRESSURE
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.util.UUID

class BloodPressureEntryUiRendererTest {
  private val ui = mock<BloodPressureEntryUi>()
  private val uiRenderer = BloodPressureEntryUiRenderer(ui)
  private val patientUuid = UUID.fromString("f5387bb5-0dbf-4d91-a487-bbe219adcd60")
  private val newBloodPressureEntryModel = BloodPressureEntryModel
      .create(New(patientUuid), LocalDate.now(TestUserClock()).year)

  @Test
  fun `when the sheet is show for a new entry, then hide remove BP button and show enter new BP title`() {
    // when
    uiRenderer.render(newBloodPressureEntryModel)

    // then
    verify(ui).hideRemoveBpButton()
    verify(ui).showEnterNewBloodPressureTitle()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when add new blood pressure entry, then show progress UI until blood pressure saved`() {
    // given
    val newBloodPressureData = newBloodPressureEntryModel.bloodPressureStateChanged(SAVING_BLOOD_PRESSURE)

    // when
    uiRenderer.render(newBloodPressureData)

    // then
    verify(ui).hideRemoveBpButton()
    verify(ui).showEnterNewBloodPressureTitle()
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }
}
