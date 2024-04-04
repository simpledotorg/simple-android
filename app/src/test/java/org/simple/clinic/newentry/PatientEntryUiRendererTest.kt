package org.simple.clinic.newentry

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory

class PatientEntryUiRendererTest {

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider())

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

  @Test
  fun `when edit patient screen has input fields, then show them in the screen`() {
    // given
    val model = PatientEntryModel.DEFAULT

    val ui = mock<PatientEntryUi>()
    val uiRenderer = PatientEntryUiRenderer(ui)

    val inputFields = InputFields(inputFieldsFactory.provideFields())
    val inputFieldsFetchedState = model.inputFieldsLoaded(inputFields)

    // when
    uiRenderer.render(inputFieldsFetchedState)

    // then
    verify(ui).setupUi(inputFields)
    verify(ui).hideIdentifierSection()
    verifyNoMoreInteractions(ui)
  }
}
