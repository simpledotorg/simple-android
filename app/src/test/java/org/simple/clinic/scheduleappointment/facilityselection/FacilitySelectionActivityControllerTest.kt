package org.simple.clinic.scheduleappointment.facilityselection

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class FacilitySelectionActivityControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val screen = mock<FacilitySelectionActivity>()

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when facility is selected then it should be passed back as result`() {
    // given
    val newFacility = TestData.facility(uuid = UUID.fromString("758f6c2e-2bd9-415f-9aaa-665923034e92"))

    // when
    setupController()
    uiEvents.onNext(FacilitySelected(newFacility))

    // then
    verify(screen).sendSelectedFacility(newFacility)
    verifyNoMoreInteractions(screen)
  }

  private fun setupController() {
    val controller = FacilitySelectionActivityController()

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }
}
