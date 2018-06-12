package org.resolvetosavelives.red.summary

import com.nhaarman.mockito_kotlin.mock
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.widgets.UiEvent

class PatientSummaryScreenControllerTest {

  private val screen = mock<PatientSummaryScreen>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: PatientSummaryScreenController

  @Before
  fun setUp() {
    controller = PatientSummaryScreenController()

    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(screen) })
  }

  @Test
  fun `when screen is opened then patient details should be set on UI`() {
  }

  @Test
  fun `when screen is opened then BP entry sheet should be shown`() {
  }

  @Test
  fun `when screen was opened from search and up button is pressed then the user should be taken back to search`() {
  }

  @Test
  fun `when screen was opened after saving a new patient and up button is pressed then the user should be taken back to home`() {
  }
}
