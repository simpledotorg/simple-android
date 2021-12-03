package org.simple.clinic.editpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientViewRendererTest {

  private val ui = mock<EditPatientUi>()
  private val uiRenderer = EditPatientViewRenderer(ui)

  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val patientProfile = TestData.patientProfile(
      patientUuid = UUID.fromString("f8193c3b-20d3-4fae-be3a-0029969db624"),
      patientAddressUuid = UUID.fromString("b7119eca-c3ab-4f28-ba88-038f737687d9"),
      generatePhoneNumber = true,
      generateBusinessId = true,
      generateDateOfBirth = true,
      dateOfBirth = LocalDate.parse("1990-03-09")
  )

  private val model = EditPatientModel.from(
      patient = patientProfile.patient,
      address = patientProfile.address,
      phoneNumber = patientProfile.phoneNumbers.first(),
      bangladeshNationalId = null,
      saveButtonState = EditPatientState.SAVING_PATIENT,
      dateOfBirthFormatter = dateOfBirthFormat
  )

  @Test
  fun `when edit patient screen has colony or villages list, then show them in the screen`() {
    // given
    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    val colonyOrVillagesFetchedState = model.updateColonyOrVillagesList(colonyOrVillages).updateDateOfBirth("2018-03-09")

    // when
    uiRenderer.render(colonyOrVillagesFetchedState)

    // then
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).setColonyOrVillagesAutoComplete(colonyOrVillages)
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when input fields are loaded, then setup the ui`() {
    // given
    val date = LocalDate.parse("2018-01-01")
    val userClock = TestUserClock(date)
    val inputFieldsList = BangladeshInputFieldsProvider(dateOfBirthFormat, LocalDate.now(userClock))
        .provide()
    val inputFields = InputFields(inputFieldsList)

    val inputFieldsLoadedModel = model.inputFieldsLoaded(inputFields)

    // when
    uiRenderer.render(inputFieldsLoadedModel)

    // then
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).showProgress()
    verify(ui).setupUi(inputFields)
    verifyNoMoreInteractions(ui)
  }
}
