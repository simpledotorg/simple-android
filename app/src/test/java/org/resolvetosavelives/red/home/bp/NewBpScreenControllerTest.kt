package org.resolvetosavelives.red.home.bp

import com.nhaarman.mockito_kotlin.mock
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.resolvetosavelives.red.widgets.UiEvent

class NewBpScreenControllerTest {

  private val screen: NewBpScreen = mock()
  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private val controller: NewBpScreenController = NewBpScreenController()

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when new patient is clicked then patient search screen should open`() {
    uiEvents.onNext(NewPatientClicked())

    verify(screen).openNewPatientScreen()
  }

  @After
  fun tearDown() {
    verifyNoMoreInteractions(screen)
  }
}
