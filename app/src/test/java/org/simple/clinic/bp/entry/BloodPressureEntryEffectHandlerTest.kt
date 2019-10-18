package org.simple.clinic.bp.entry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC

class BloodPressureEntryEffectHandlerTest {
  private val ui = mock<BloodPressureEntryUi>()
  private val userClock = TestUserClock()
  private val effectHandler = BloodPressureEntryEffectHandler
      .create(ui, userClock, UserInputDatePaddingCharacter.ZERO, mock(), TrampolineSchedulersProvider())
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun teardown() {
    testCase.dispose()
  }

  @Test
  fun `when prefill date is dispatched, then populate date button and date input fields`() {
    // when
    val entryDate = LocalDate.of(1992, 6, 7)
    userClock.setDate(LocalDate.of(1992, 6, 7), UTC)
    testCase.dispatch(PrefillDate.forNewEntry())

    // then
    verify(ui).setDateOnInputFields("07", "06", "92")
    verify(ui).showDateOnDateButton(entryDate)
    testCase.assertNoOutgoingEvents()
  }
}
