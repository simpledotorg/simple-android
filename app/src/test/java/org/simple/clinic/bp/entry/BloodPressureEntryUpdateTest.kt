package org.simple.clinic.bp.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicEmpty
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class BloodPressureEntryUpdateTest {

  private val patientUuid = UUID.fromString("6fc16e72-39a5-4568-86db-1f8b1c0c08d3")
  private val defaultModel = BloodPressureEntryModel.create(OpenAs.New(patientUuid), 2018)

  private val clock = TestUserClock()
  private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val update = BloodPressureEntryUpdate(
      BpValidator(),
      UserInputDateValidator(clock, dateFormatter),
      LocalDate.now(clock),
      UserInputDatePaddingCharacter.ZERO
  )

  private val spec = UpdateSpec(update)

  @Test
  fun `when the systolic entry is blank, the error message must be shown when clicking save`() {
    val model = defaultModel
        .systolicChanged("")
        .diastolicChanged("80")

    spec
        .given(model)
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBpValidationError(ErrorSystolicEmpty) as BloodPressureEntryEffect)
        ))
  }

  @Test
  fun `when the diastolic entry is blank, the error message must be shown when clicking save`() {
    val model = defaultModel
        .systolicChanged("120")
        .diastolicChanged("")

    spec
        .given(model)
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBpValidationError(ErrorDiastolicEmpty) as BloodPressureEntryEffect)
        ))
  }
}
