package org.simple.clinic.newentry

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.ReminderConsent.Denied
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.registration.phone.IndianPhoneNumberValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class PatientEntryUpdateTest {
  private val phoneNumberValidator = IndianPhoneNumberValidator()
  private val dobValidator = UserInputDateValidator(ZoneOffset.UTC, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
  private val update = PatientEntryUpdate(phoneNumberValidator, dobValidator)
  private val updateSpec = UpdateSpec(update)

  @Test
  fun `when the user grants reminder consent, update the model`() {
    val defaultModel = PatientEntryModel.DEFAULT
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
    val defaultModel = PatientEntryModel.DEFAULT
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
}
