package org.simple.clinic.home.overdue

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.widgets.UiEvent

class OverdueScreenControllerTest {

  private val screen = mock<OverdueScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val appointmentRepo = mock<AppointmentRepository>()

  lateinit var controller: OverdueScreenController

  @Before
  fun setUp() {
    controller = OverdueScreenController(appointmentRepo)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created, and overdue list is retrieved, show it`() {
    whenever(appointmentRepo.overdueAppointments()).thenReturn(Observable.just(listOf(PatientMocker.appointment())))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(any())
    verify(screen).handleEmptyList(false)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when screen is created, and overdue list is empty, show empty list UI`() {
    whenever(appointmentRepo.overdueAppointments()).thenReturn(Observable.just(listOf()))

    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateList(any())
    verify(screen).handleEmptyList(true)
    verifyNoMoreInteractions(screen)
  }
}
