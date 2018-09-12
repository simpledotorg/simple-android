package org.simple.clinic.scheduleappointment

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class ScheduleAppointmentSheetControllerTest {

  private val sheet = mock<ScheduleAppointmentSheet>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: ScheduleAppointmentSheetController
  private val repository = mock<AppointmentRepository>()

  @Before
  fun setUp() {
    controller = ScheduleAppointmentSheetController(repository)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(sheet) }
  }

  @Test
  fun `when sheet is created, a date should immediately be displayed to the user`() {
    val current = 17
    uiEvents.onNext(SheetCreated(current))

    verify(sheet).updateDisplayedDate(current)
  }

  @Test
  @Parameters(method = "paramsForIncrementingAndDecrementing")
  fun `when increment button is clicked, appointment due date should increase`(
      current: Int,
      size: Int
  ) {
    uiEvents.onNext(IncrementAppointmentDate(current, size))

    if (current == size - 1) {
      verify(sheet).enableIncrementButton(false)
    } else {
      verify(sheet).updateDisplayedDate(current + 1)
      verify(sheet).enableIncrementButton(true)
    }
  }

  @Test
  @Parameters(method = "paramsForIncrementingAndDecrementing")
  fun `when decrement button is clicked, appointment due date should decrease`(
      current: Int,
      size: Int
  ) {
    uiEvents.onNext(DecrementAppointmentDate(current))

    if (current != 0) {
      verify(sheet).updateDisplayedDate(current - 1)
      verify(sheet).enableDecrementButton(true)
    } else {
      verify(sheet).enableDecrementButton(false)
    }
  }

  fun paramsForIncrementingAndDecrementing() = arrayOf(
      arrayOf(3, 9),
      arrayOf(0, 9),
      arrayOf(8, 9),
      arrayOf(7, 9),
      arrayOf(6, 9)
  )

  @Test
  fun `when not-now is clicked, appointment should not be scheduled, and sheet should dismiss`() {
    uiEvents.onNext(SkipScheduling())

    verify(repository, never()).schedule(any(), any())
    verify(sheet).closeSheet()
  }
}
