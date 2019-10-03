package org.simple.clinic.newentry.clearbutton

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

class ClearFieldImageButtonControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val button = mock<ClearFieldImageButton>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: ClearFieldImageButtonController

  @Before
  fun setUp() {
    controller = ClearFieldImageButtonController()

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(button) }
  }

  @Test
  fun `clear button should remain visible only while the associated field is focused and is not blank`() {
    with(uiEvents) {
      onNext(CleareableFieldFocusChanged(hasFocus = false))
      onNext(CleareableFieldTextChanged(""))
      onNext(CleareableFieldFocusChanged(hasFocus = true))
      onNext(CleareableFieldTextChanged("A"))
      onNext(CleareableFieldTextChanged("AB"))
      onNext(CleareableFieldFocusChanged(hasFocus = false))
    }

    verify(button, times(2)).setVisible(false)
    verify(button, times(1)).setVisible(true)
  }
}
