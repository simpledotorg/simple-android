package org.simple.clinic.drugs.selection.custom

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.drugs.search.DrugFrequency
import java.util.UUID

class CustomDrugEntryUiRendererTest {

  private val ui = mock<CustomDrugEntryUi>()
  private val uiRenderer = CustomDrugEntryUiRenderer(ui)
  private val drugName = "Amlodipine"
  private val patientUuid = UUID.fromString("77f1d870-5c60-49f7-a4e2-2f1d60e4218c")
  private val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New(patientUuid), drug = null, drugName = drugName)

  @Test
  fun `when drug frequency is changed, then set the new drug frequency`() {
    // given
    val frequency = DrugFrequency.OD
    val drugFrequencyChangedModel = defaultModel.frequencyEdited(frequency)

    // when
    uiRenderer.render(drugFrequencyChangedModel)

    // then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugName(drugName)
    verify(ui).setDrugFrequency(frequency)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug dosage is changed, then set the new drug dosage`() {
    // given
    val dosage = "15 mg"
    val drugDosageChangedModel = defaultModel.dosageEdited(dosage)

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugName(drugName)
    verify(ui).setDrugDosage(dosage)
    verify(ui).setDrugFrequency(DrugFrequency.Unknown("None"))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the screen is loaded in create mode, then render the drug name and setup ui for creating drug entry`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugName(drugName)
    verify(ui).setDrugFrequency(DrugFrequency.Unknown("None"))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the screen is loaded in update mode, then render the drug name and setup ui for updating drug entry`() {
    // given
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.Update(patientUuid, prescribedDrugUuid), drug = null, drugName = drugName)

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showRemoveButton()
    verify(ui).setButtonTextAsSave()
    verify(ui).setDrugName(drugName)
    verify(ui).setDrugFrequency(DrugFrequency.Unknown("None"))
    verifyNoMoreInteractions(ui)
  }
}
