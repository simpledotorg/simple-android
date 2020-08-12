package org.simple.clinic.editpatient

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
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

  @Test
  fun `when the input fields are loaded, the UI must be setup`() {
    val clock = TestUserClock(LocalDate.parse("2018-01-01"))
    val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

    val numberValidator = LengthBasedNumberValidator(
        minimumRequiredLengthMobile = 10,
        maximumAllowedLengthMobile = 10,
        minimumRequiredLengthLandlinesOrMobile = 6,
        maximumAllowedLengthLandlinesOrMobile = 12
    )
    val dobValidator = UserInputDateValidator(
        userClock = clock,
        dateOfBirthFormat = dateOfBirthFormat
    )
    val ageValidator = UserInputAgeValidator(
        userClock = clock,
        dateOfBirthFormat = dateOfBirthFormat
    )

    val india = TestData.country(isoCountryCode = Country.INDIA)

    val inputFieldsFactory = InputFieldsFactory(
        dateTimeFormatter = dateOfBirthFormat,
        today = LocalDate.now(clock),
        country = india
    )

    val patientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("f8193c3b-20d3-4fae-be3a-0029969db624"),
        patientAddressUuid = UUID.fromString("b7119eca-c3ab-4f28-ba88-038f737687d9"),
        generatePhoneNumber = true,
        generateBusinessId = true
    )

    val model = EditPatientModel.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.first(),
        bangladeshNationalId = null,
        saveButtonState = EditPatientState.NOT_SAVING_PATIENT,
        dateOfBirthFormatter = dateOfBirthFormat
    )

    val spec = UpdateSpec(EditPatientUpdate(
        numberValidator = numberValidator,
        dobValidator = dobValidator,
        ageValidator = ageValidator
    ))

    val inputFields = InputFields(inputFieldsFactory.provideFields())

    spec
        .given(model)
        .whenEvent(InputFieldsLoaded(inputFields))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetupUi(inputFields) as EditPatientEffect)
        ))
  }
}
