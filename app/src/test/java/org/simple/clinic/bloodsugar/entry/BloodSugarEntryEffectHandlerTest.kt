package org.simple.clinic.bloodsugar.entry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooLow
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.threeten.bp.LocalDate

class BloodSugarEntryEffectHandlerTest {

  private val ui = mock<BloodSugarEntryUi>()
  private val userClock = TestUserClock()

  private val effectHandler = BloodSugarEntryEffectHandler(
      ui,
      userClock,
      TrampolineSchedulersProvider()
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `blood sugar error message must be hidden when hide blood sugar error message effect is received`() {
    // when
    testCase.dispatch(HideBloodSugarErrorMessage)

    // then
    verify(ui).hideBloodSugarErrorMessage()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `date error message must be hidden when hide date error message effect is received`() {
    // when
    testCase.dispatch(HideDateErrorMessage)

    // then
    verify(ui).hideDateErrorMessage()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `blood sugar entry sheet must be dismissed when dismiss effect is received`() {
    // when
    testCase.dispatch(Dismiss)

    // then
    verify(ui).dismiss()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show date entry screen when show date entry screen effect is received`() {
    // when
    testCase.dispatch(ShowDateEntryScreen)

    // then
    verify(ui).showDateEntryScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show blood sugar empty error when show blood sugar validation error effect is received with validation result empty`() {
    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarEmpty))

    // then
    verify(ui).showBloodSugarEmptyError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show blood sugar high error when show blood sugar validation error effect is received with validation result too high`() {
    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarTooHigh))

    // then
    verify(ui).showBloodSugarHighError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show blood sugar low error when show blood sugar validation error effect is received with validation result too low`() {
    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarTooLow))

    // then
    verify(ui).showBloodSugarLowError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show blood sugar entry screen when show blood sugar entry screen effect is received`() {
    // given
    val bloodSugarDate = LocalDate.of(2020, 1, 1)

    // when
    testCase.dispatch(ShowBloodSugarEntryScreen(bloodSugarDate))

    // then
    verify(ui).showBloodSugarEntryScreen()
    verify(ui).showDateOnDateButton(bloodSugarDate)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `set date on input fields and date button when prefill date effect is received`() {
    // given
    val entryDate = LocalDate.of(1992, 6, 7)
    userClock.setDate(LocalDate.of(1992, 6, 7))

    // when
    testCase.dispatch(PrefillDate.forNewEntry())

    // then
    testCase.assertOutgoingEvents(DatePrefilled(entryDate))
    verify(ui).setDateOnInputFields("7", "6", "92")
    verify(ui).showDateOnDateButton(entryDate)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show invalid date error when show date validation error is received with validation invalid pattern`() {
    // when
    testCase.dispatch(ShowDateValidationError(InvalidPattern))

    // then
    verify(ui).showInvalidDateError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show date is in future error when show date validation error is received with validation date is in future`() {
    // when
    testCase.dispatch(ShowDateValidationError(DateIsInFuture))

    // then
    verify(ui).showDateIsInFutureError()
    verifyNoMoreInteractions(ui)
  }
}
