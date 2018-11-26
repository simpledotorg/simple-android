package org.simple.clinic.facility.change

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class FacilityChangeScreenControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<FacilityChangeScreen>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()

  private lateinit var controller: FacilityChangeScreenController

  @Before
  fun setUp() {
    controller = FacilityChangeScreenController(facilityRepository, userSession)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then facilities UI models should be created`() {
    val facility1 = PatientMocker.facility()
    val facility2 = PatientMocker.facility()
    val facilities = listOf(facility1, facility2)
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities, facilities))

    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = ""))

    verify(screen).updateFacilities(listOf(facility1, facility2), isFirstUpdate = true)
    verify(screen).updateFacilities(listOf(facility1, facility2), isFirstUpdate = false)
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities("F")).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.facilities("Fa")).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.facilities("Fac")).thenReturn(Observable.just(facilities))

    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "F"))
    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "Fa"))
    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "Fac"))

    verify(facilityRepository).facilities("F")
    verify(facilityRepository).facilities("Fa")
    verify(facilityRepository).facilities("Fac")
  }

  @Test
  fun `when a facility is selected then the user's facility should be changed and the screen should be closed`() {
    val newFacility = PatientMocker.facility()
    val user = PatientMocker.loggedInUser()
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(facilityRepository.associateUserWithFacility(user, newFacility)).thenReturn(Completable.complete())
    whenever(facilityRepository.setCurrentFacility(user, newFacility)).thenReturn(Completable.complete())

    uiEvents.onNext(FacilityChangeClicked(newFacility))

    val inOrder = inOrder(facilityRepository, screen)
    inOrder.verify(facilityRepository).associateUserWithFacility(user, newFacility)
    inOrder.verify(screen).goBack()
  }
}
