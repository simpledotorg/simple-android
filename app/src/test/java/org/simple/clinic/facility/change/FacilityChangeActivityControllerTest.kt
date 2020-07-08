package org.simple.clinic.facility.change

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class FacilityChangeActivityControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<FacilityChangeActivity>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()

  private val user = TestData.loggedInUser(uuid = UUID.fromString("43ea80dc-7aff-4679-b714-c4e74c529f84"))
  private val currentFacility = TestData.facility(uuid = UUID.fromString("6dc536d9-b460-4143-9b3b-7caedf17c0d9"))

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when a new facility is selected then the confirmation sheet to change facility should open`() {
    //given
    val newFacility = TestData.facility(uuid = UUID.fromString("ce22e8b1-eba2-463f-8e91-0c237ebebf6b"))

    //when
    setupController()
    uiEvents.onNext(FacilityChangeClicked(newFacility))

    //then
    verify(screen).openConfirmationSheet(newFacility)
  }

  @Test
  fun `when the same facility is selected then the sheet should close`() {
    //given
    val newFacility = currentFacility

    //when
    setupController()
    uiEvents.onNext(FacilityChangeClicked(newFacility))

    //then
    verify(screen).goBack()
  }

  private fun setupController() {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(facilityRepository.currentFacility(user)) doReturn Observable.just(currentFacility)

    val controller = FacilityChangeActivityController(
        facilityRepository = facilityRepository,
        userSession = userSession
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(ScreenCreated())
  }
}
