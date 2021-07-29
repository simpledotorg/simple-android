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

  private val defaultModel = CustomDrugEntryModel.default(drug = null, drugName = drugName)

  @Test
  fun `when drug frequency is changed, then set the new drug frequency`() {
    // given
    val frequency = DrugFrequency.OD
    val drugFrequencyChangedModel = defaultModel.frequencyEdited(frequency)

    // when
    uiRenderer.render(drugFrequencyChangedModel)

    // then
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
    verify(ui).setDrugDosage(dosage)
    verify(ui).setDrugFrequency(DrugFrequency.Unknown("None"))
    verifyNoMoreInteractions(ui)
  }
}
