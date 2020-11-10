package org.simple.clinic.bloodsugar.entry

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.entry.BloodSugarSaveState.SAVING_BLOOD_SUGAR
import org.simple.clinic.bloodsugar.entry.OpenAs.New
import org.simple.clinic.bloodsugar.entry.OpenAs.Update
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.util.UUID

class BloodSugarEntryUiRendererTest {

  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")
  private val bloodSugarUuid = UUID.fromString("27a522a0-4896-4875-a1cb-14baa7c2d7a9")

  private val ui = mock<BloodSugarEntryUi>()
  private val bloodSugarEntryUiRenderer = BloodSugarEntryUiRenderer(ui)
  private val testUserClock = TestUserClock()
  private val year = LocalDate.now(testUserClock).year
  private val bloodSugarEntryModel = BloodSugarEntryModel.create(year, New(patientUuid, Random))

  @Test
  fun `it should render the entry title when creating a new blood sugar`() {
    // when
    bloodSugarEntryUiRenderer.render(bloodSugarEntryModel)

    // then
    verify(ui).hideRemoveButton()
    verify(ui).showEntryTitle(Random)
    verify(ui).hideProgress()
    verify(ui).setBloodSugarUnitPreferenceLabelToMg()
    verify(ui).showBloodSugarUnitPreferenceButton()
    verify(ui).hideBloodSugarUnitPreferenceLabel()
    verify(ui).numericBloodSugarInputType()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `it should render the edit title when updating a blood sugar`() {
    // given
    val bloodSugarEntryModel = BloodSugarEntryModel.create(year, Update(bloodSugarUuid, Random))

    // when
    bloodSugarEntryUiRenderer.render(bloodSugarEntryModel)

    // then
    verify(ui).showRemoveButton()
    verify(ui).showEditTitle(Random)
    verify(ui).hideProgress()
    verify(ui).setBloodSugarUnitPreferenceLabelToMg()
    verify(ui).showBloodSugarUnitPreferenceButton()
    verify(ui).hideBloodSugarUnitPreferenceLabel()
    verify(ui).numericBloodSugarInputType()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when add new blood sugar entry, then show progress UI until blood sugar saved`() {
    // given
    val newBloodSugarEntryModel = bloodSugarEntryModel.bloodSugarStateChanged(SAVING_BLOOD_SUGAR)

    // when
    bloodSugarEntryUiRenderer.render(newBloodSugarEntryModel)

    // then
    verify(ui).hideRemoveButton()
    verify(ui).showEntryTitle(Random)
    verify(ui).showProgress()
    verify(ui).setBloodSugarUnitPreferenceLabelToMg()
    verify(ui).showBloodSugarUnitPreferenceButton()
    verify(ui).hideBloodSugarUnitPreferenceLabel()
    verify(ui).numericBloodSugarInputType()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the blood sugar preference is changed, then update the UI`() {
    // given
    val newBloodSugarEntryModel = bloodSugarEntryModel.bloodSugarUnitChanged(BloodSugarUnitPreference.Mmol)

    // when
    bloodSugarEntryUiRenderer.render(newBloodSugarEntryModel)

    // then
    verify(ui).hideRemoveButton()
    verify(ui).showEntryTitle(Random)
    verify(ui).hideProgress()
    verify(ui).setBloodSugarUnitPreferenceLabelToMmol()
    verify(ui).showBloodSugarUnitPreferenceButton()
    verify(ui).hideBloodSugarUnitPreferenceLabel()
    verify(ui).decimalOrNumericBloodSugarInputType()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the blood sugar unit preference is changed, update the input type to support decimal type as well`() {
    // given
    val newBloodSugarEntryModel = bloodSugarEntryModel.bloodSugarUnitChanged(BloodSugarUnitPreference.Mmol)

    // when
    bloodSugarEntryUiRenderer.render(newBloodSugarEntryModel)

    // then
    verify(ui).hideRemoveButton()
    verify(ui).showEntryTitle(Random)
    verify(ui).hideProgress()
    verify(ui).setBloodSugarUnitPreferenceLabelToMmol()
    verify(ui).showBloodSugarUnitPreferenceButton()
    verify(ui).hideBloodSugarUnitPreferenceLabel()
    verify(ui).decimalOrNumericBloodSugarInputType()
    verifyNoMoreInteractions(ui)
  }
}
