package org.simple.clinic.newentry

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test

class PatientEntryUiRendererTest {
  @Test
  fun `when edit patient screen has colony or villages list, then show them in the screen`() {
    // given
    val model = PatientEntryModel.DEFAULT

    val ui = mock<PatientEntryUi>()
    val uiRenderer = PatientEntryUiRenderer(ui)

    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    val colonyOrVillagesFetchedState = model.colonyOrVillageListUpdated(colonyOrVillages)

    // when
    uiRenderer.render(colonyOrVillagesFetchedState)

    // then
    verify(ui).setColonyOrVillagesAutoComplete(colonyOrVillages)
    verify(ui).hideIdentifierSection()
    verifyNoMoreInteractions(ui)
  }
}
