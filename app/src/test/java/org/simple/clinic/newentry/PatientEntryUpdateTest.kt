package org.simple.clinic.newentry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientEntryValidationError
import org.simple.clinic.patient.ReminderConsent.Denied
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.registration.phone.IndianPhoneNumberValidator
import org.simple.clinic.util.Just
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class PatientEntryUpdateTest {
  private val phoneNumberValidator = IndianPhoneNumberValidator()
  private val userClock: UserClock = TestUserClock()
  private val dobValidator = UserInputDateValidator(userClock, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
  private val ageValidator = UserInputAgeValidator(userClock, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
  private val update = PatientEntryUpdate(phoneNumberValidator, dobValidator, ageValidator)
  private val updateSpec = UpdateSpec(update)
  private val defaultModel = PatientEntryModel.DEFAULT

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
    //given
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.FULL_NAME_EMPTY)
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

    updateSpec
        .given(givenModel)
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
    //given
    val errors: List<PatientEntryValidationError> = listOf(PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT)
    val givenModel = defaultModel
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
        .given(givenModel)
        .`when`(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowValidationErrors(errors) as PatientEntryEffect)
            )
        )
  }

}
