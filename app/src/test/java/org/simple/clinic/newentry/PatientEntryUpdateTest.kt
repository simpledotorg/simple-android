package org.simple.clinic.newentry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.MAX_ALLOWED_PATIENT_AGE
import org.simple.clinic.MIN_ALLOWED_PATIENT_AGE
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientEntryValidationError
import org.simple.clinic.patient.ReminderConsent.Denied
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.registration.phone.LengthBasedNumberValidator
import org.simple.clinic.util.Just
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PatientEntryUpdateTest {
  private val phoneNumberValidator = LengthBasedNumberValidator(
      minimumRequiredLengthMobile = 10,
      maximumAllowedLengthMobile = 10,
      minimumRequiredLengthLandlinesOrMobile = 6,
      maximumAllowedLengthLandlinesOrMobile = 12
  )

  private val localDate = LocalDate.parse("2020-01-01")
  private val userClock: UserClock = TestUserClock(localDate)
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val dobValidator = UserInputDateValidator(userClock, dateOfBirthFormat)
  private val ageValidator = UserInputAgeValidator(userClock, dateOfBirthFormat)
  private val update = PatientEntryUpdate(phoneNumberValidator, dobValidator, ageValidator)
  private val updateSpec = UpdateSpec(update)
  private val defaultModel = PatientEntryModel.DEFAULT

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider(
      dateTimeFormatter = dateOfBirthFormat,
      today = LocalDate.now(userClock)
  ))

  @Test
  fun `when the user grants reminder consent, update the model`() {
    val granted = Granted

    updateSpec
        .given(defaultModel)
        .`when`(ReminderConsentChanged(granted))
        .then(
            assertThatNext(
                hasModel(defaultModel.reminderConsentChanged(granted)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when the user denies reminder consent, update the model`() {
    val denied = Denied

    updateSpec
        .given(defaultModel)
        .`when`(ReminderConsentChanged(denied))
        .then(
            assertThatNext(
                hasModel(defaultModel.reminderConsentChanged(denied)),
                hasNoEffects()
            )
        )
  }

  //Tests for validation errors in Patient Entry Screen
  @Test
  fun `when the user leaves full name field empty, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.FullNameEmpty)
    val model = defaultModel
        .fullNameChanged("")
        .ageChanged("21")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the user enters phone number which is too short, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.PhoneNumberLengthTooShort(6))
    val model = defaultModel
        .fullNameChanged("Name")
        .ageChanged("21")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("77210")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the user enters phone number which is too long, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.PhoneNumberLengthTooLong(12))
    val model = defaultModel
        .fullNameChanged("Name")
        .ageChanged("21")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721083838380")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }


  @Test
  fun `when the user doesn't enter date of birth and age, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.BothDateOfBirthAndAgeAbsent)
    val model = defaultModel
        .fullNameChanged("Name")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the user enters invalid date of birth, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.InvalidDateOfBirth)
    val model = defaultModel
        .fullNameChanged("Name")
        .dateOfBirthChanged("02-19-2000")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the user enters date of birth in future, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.DateOfBirthInFuture)
    val model = defaultModel
        .fullNameChanged("Name")
        .dateOfBirthChanged("02/02/2021")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the user leaves gender field empty, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.MissingGender)
    val model = defaultModel
        .fullNameChanged("Name")
        .ageChanged("12")
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the user leaves colony or village field empty, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.ColonyOrVillageEmpty)
    val model = defaultModel
        .fullNameChanged("Name")
        .ageChanged("12")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the user leaves district field empty, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.DistrictEmpty)
    val model = defaultModel
        .fullNameChanged("Name")
        .ageChanged("12")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the user leaves state field empty, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.StateEmpty)
    val model = defaultModel
        .fullNameChanged("Name")
        .ageChanged("12")
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the age exceeds max limit, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.AgeExceedsMaxLimit)
    val model = defaultModel
        .fullNameChanged("Name")
        .ageChanged(MAX_ALLOWED_PATIENT_AGE.plus(1).toString())
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the age exceeds min limit, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.AgeExceedsMinLimit)
    val model = defaultModel
        .fullNameChanged("Name")
        .ageChanged(MIN_ALLOWED_PATIENT_AGE.minus(1).toString())
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the date of birth exceeds max limit, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.DobExceedsMaxLimit)
    val enteredDate = dateOfBirthFormat.format(LocalDate.now(userClock).minusYears(MAX_ALLOWED_PATIENT_AGE.toLong().plus(1)))
    val model = defaultModel
        .fullNameChanged("Name")
        .dateOfBirthChanged(enteredDate)
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the date of birth exceeds min limit, then show error`() {
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.DobExceedsMinLimit)
    val enteredDate = dateOfBirthFormat.format(LocalDate.now(userClock))
    val model = defaultModel
        .fullNameChanged("Name")
        .dateOfBirthChanged(enteredDate)
        .genderChanged(Just(Gender.Male))
        .phoneNumberChanged("7721084840")
        .streetAddressChanged("street")
        .colonyOrVillageChanged("village")
        .districtChanged("district")
        .stateChanged("state")
        .zoneChanged("zone")

    updateSpec
        .given(model)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

  @Test
  fun `when the input fields are loaded, the UI muse be initialized`() {
    val inputFields = InputFields(inputFieldsFactory.provideFields())

    updateSpec
        .given(defaultModel)
        .whenEvent(InputFieldsLoaded(inputFields))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetupUi(inputFields) as PatientEntryEffect)
        ))
  }

  @Test
  fun `when the colony or villages list is updated, then update the model`() {
    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    updateSpec
        .given(defaultModel)
        .whenEvent(ColonyOrVillagesFetched(colonyOrVillages))
        .then(assertThatNext(
            hasModel(defaultModel.colonyOrVillageListUpdated(colonyOrVillages)),
            hasNoEffects()
        ))
  }
}
