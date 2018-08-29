package org.simple.clinic.home.overdue

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.widgets.UiEvent

class OverdueScreenControllerTest {

  private val screen = mock<OverdueScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  lateinit var controller: OverdueScreenController

  @Before
  fun setUp() {
    controller = OverdueScreenController()

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created, show list of overdue patients`() {
    uiEvents.onNext(OverdueScreenCreated())

    verify(screen).updateOverdueList()
  }

  @Test
  fun `when overdue list is empty, show message to user`() {

  }
}
