package org.simple.clinic.textInputdatepicker

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.HideDateErrorMessage
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.UserEnteredDateSelected
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.DateIsInPast
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.InvalidPattern
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.MaximumAllowedDateRange
import java.time.LocalDate

class TextInputDatePickerEffectHandlerTest {

  private val userClock = TestUserClock()
  private val uiActions = mock<TextInputDatePickerUiActions>()
  private val effectHandler = TextInputDatePickerEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions,
      userClock = userClock
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)
  
  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when close sheet effect is received, then dismiss the sheet`() {
    // when
    effectHandlerTestCase.dispatch(DismissSheet)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).dismissSheet()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when hide date error message is received, then hide the error text`() {
    // when
    effectHandlerTestCase.dispatch(HideDateErrorMessage)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).hideErrorMessage()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show validation error effect is received, then show invalid date error`() {
    // given
    val validationResult = InvalidPattern

    // when
    effectHandlerTestCase.dispatch(TextInputDatePickerEffect.ShowDateValidationError(validationResult))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).showInvalidDateError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show validation error effect is received, then show date is in past error`() {
    // given
    val validationResult = DateIsInPast

    // when
    effectHandlerTestCase.dispatch(TextInputDatePickerEffect.ShowDateValidationError(validationResult))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).showDateIsInPastError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show validation error effect is received, then date cannot be after one year error`() {
    // given
    val validationResult = MaximumAllowedDateRange

    // when
    effectHandlerTestCase.dispatch(TextInputDatePickerEffect.ShowDateValidationError(validationResult))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).showMaximumDateRangeError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when send user entered date effect is received, then send the entered date back to the previous screen`() {
    // given
    val userEnteredDate = LocalDate.parse("2021-04-05")

    // when
    effectHandlerTestCase.dispatch(UserEnteredDateSelected(userEnteredDate = userEnteredDate))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).userEnteredDateSelected(userEnteredDate)
  }
}
