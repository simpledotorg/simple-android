package org.resolvetosavelives.red.newentry.clearbutton

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.widgets.UiEvent

class ClearFieldImageButtonControllerTest {

  private val button = mock<ClearFieldImageButton>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: ClearFieldImageButtonController

  @Before
  fun setUp() {
    controller = ClearFieldImageButtonController()

    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(button) })
  }

  @Test
  fun `clear button should remain visible only while the associated field is not blank`() {
    uiEvents.onNext(CleareableFieldTextChanged(""))
    uiEvents.onNext(CleareableFieldTextChanged("A"))
    uiEvents.onNext(CleareableFieldTextChanged("AB"))

    verify(button).setVisible(false)
    verify(button, times(1)).setVisible(true)
  }
}
