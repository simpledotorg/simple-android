package org.simple.clinic.newentry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientEntryValidationError
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility

class PatientEntryUiRendererTest {
  private val defaultModel = PatientEntryModel.DEFAULT
  private val patientEntryUi = mock<PatientEntryUi>()
  private val patientEntryUiRenderer = PatientEntryUiRenderer(patientEntryUi)

  @Test
  fun `it should render full name empty error`() {
    //given
    val error: PatientEntryValidationError = PatientEntryValidationError.FULL_NAME_EMPTY
    val givenModel = defaultModel
        .fullNameChanged("")
        .ageChanged("21")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")
        .validationFailed(error)

    //when
    patientEntryUiRenderer.render(givenModel)

    //then
    verify(patientEntryUi).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)
    verify(patientEntryUi).hideIdentifierSection()
    verify(patientEntryUi).showValidationErrorUi(error)
    verifyNoMoreInteractions(patientEntryUi)
  }

  @Test
  fun `it should render phone number length too short error`() {
    //given
    val error: PatientEntryValidationError = PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
    val givenModel = defaultModel
        .fullNameChanged("Name")
        .ageChanged("21")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")
        .validationFailed(error)

    //when
    patientEntryUiRenderer.render(givenModel)

    //then
    verify(patientEntryUi).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)
    verify(patientEntryUi).hideIdentifierSection()
    verify(patientEntryUi).showValidationErrorUi(error)
    verifyNoMoreInteractions(patientEntryUi)
  }

  @Test
  fun `it should render phone number length too long error`() {
    //given
    val error: PatientEntryValidationError = PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
    val givenModel = defaultModel
        .fullNameChanged("Name")
        .ageChanged("21")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")
        .validationFailed(error)

    //when
    patientEntryUiRenderer.render(givenModel)

    //then
    verify(patientEntryUi).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)
    verify(patientEntryUi).hideIdentifierSection()
    verify(patientEntryUi).showValidationErrorUi(error)
    verifyNoMoreInteractions(patientEntryUi)
  }

  @Test
  fun `it should render both date of birth and age absent error`() {
    //given
    val error: PatientEntryValidationError = PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
    val givenModel = defaultModel
        .fullNameChanged("Name")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("772184840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")
        .validationFailed(error)

    //when
    patientEntryUiRenderer.render(givenModel)

    //then
    verify(patientEntryUi).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.BOTH_VISIBLE)
    verify(patientEntryUi).hideIdentifierSection()
    verify(patientEntryUi).showValidationErrorUi(error)
    verifyNoMoreInteractions(patientEntryUi)
  }

  @Test
  fun `it should render invalid date of birth error`() {
    //given
    val error: PatientEntryValidationError = PatientEntryValidationError.INVALID_DATE_OF_BIRTH
    val givenModel = defaultModel
        .fullNameChanged("Name")
        .dateOfBirthChanged("12/34-2019")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("772184840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")
        .validationFailed(error)

    //when
    patientEntryUiRenderer.render(givenModel)

    //then
    verify(patientEntryUi).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE)
    verify(patientEntryUi).hideIdentifierSection()
    verify(patientEntryUi).showValidationErrorUi(error)
    verifyNoMoreInteractions(patientEntryUi)
  }
}