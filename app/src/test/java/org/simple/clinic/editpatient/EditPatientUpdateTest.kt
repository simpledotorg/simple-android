package org.simple.clinic.editpatient

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.registration.phone.LengthBasedNumberValidator
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientUpdateTest {
  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val numberValidator = LengthBasedNumberValidator(
      minimumRequiredLengthMobile = 10,
      maximumAllowedLengthMobile = 10,
      minimumRequiredLengthLandlinesOrMobile = 6,
      maximumAllowedLengthLandlinesOrMobile = 12
  )
  private val dobValidator = UserInputDateValidator(
      userClock = clock,
      dateOfBirthFormat = dateOfBirthFormat
  )
  private val ageValidator = UserInputAgeValidator(
      userClock = clock,
      dateOfBirthFormat = dateOfBirthFormat
  )
  private val updateSpec = UpdateSpec(EditPatientUpdate(
      numberValidator = numberValidator,
      dobValidator = dobValidator,
      ageValidator = ageValidator
  ))

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider(
      dateTimeFormatter = dateOfBirthFormat,
      today = LocalDate.now(clock)
  ))

  private val patientProfile = TestData.patientProfile(
      patientUuid = UUID.fromString("f8193c3b-20d3-4fae-be3a-0029969db624"),
      patientAddressUuid = UUID.fromString("b7119eca-c3ab-4f28-ba88-038f737687d9"),
      generatePhoneNumber = true,
      generateBusinessId = true
  )

  private val model = EditPatientModel.from(
      patient = patientProfile.patient,
      address = patientProfile.address,
      phoneNumber = patientProfile.phoneNumbers.first(),
      bangladeshNationalId = null,
      saveButtonState = EditPatientState.NOT_SAVING_PATIENT,
      dateOfBirthFormatter = dateOfBirthFormat
  )

  @Test
  fun `when the input fields are loaded, the UI must be setup`() {
    val inputFields = InputFields(inputFieldsFactory.provideFields())

    updateSpec
        .given(model)
        .whenEvent(InputFieldsLoaded(inputFields))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetupUi(inputFields) as EditPatientEffect)
        ))
  }

  @Test
  fun `when the colony or villages list is updated, then update the model`() {
    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    updateSpec
        .given(model)
        .whenEvent(ColonyOrVillagesFetched(colonyOrVillages))
        .then(assertThatNext(
            hasModel(model.updateColonyOrVillagesList(colonyOrVillages)),
            hasNoEffects()
        ))
  }
}
