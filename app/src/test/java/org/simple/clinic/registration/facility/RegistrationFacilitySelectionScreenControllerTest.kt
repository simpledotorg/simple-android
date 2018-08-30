package org.simple.clinic.registration.facility

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.registration.RegistrationScheduler
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.UUID

class RegistrationFacilitySelectionScreenControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<RegistrationFacilitySelectionScreen>()
  private val facilitySync = mock<FacilitySync>()
  private val facilityRepository = mock<FacilityRepository>()
  private val registrationScheduler = mock<RegistrationScheduler>()
  private val userSession = mock<UserSession>()

  private lateinit var controller: RegistrationFacilitySelectionScreenController

  @Before
  fun setUp() {
    controller = RegistrationFacilitySelectionScreenController(facilitySync, facilityRepository, userSession, registrationScheduler)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is started then facilities should be fetched if they are empty`() {
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(emptyList()))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showProgressIndicator()
    verify(facilitySync).pullWithResult()
    verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when screen is started then facilities should not be fetched if they are already available`() {
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(listOf(PatientMocker.facility())))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success()))

    uiEvents.onNext(ScreenCreated())

    verify(screen, never()).showProgressIndicator()
    verify(facilitySync, never()).pullWithResult()
    verify(screen, never()).hideProgressIndicator()
  }

  @Test
  fun `when fetching facilities fails then an error should be shown`() {
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(emptyList()))
    whenever(facilitySync.pullWithResult())
        .thenReturn(Single.just(FacilityPullResult.UnexpectedError()))
        .thenReturn(Single.just(FacilityPullResult.NetworkError()))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilitySelectionRetryClicked())

    verify(screen).showNetworkError()
    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when retry is clicked then the error should be cleared and facilities should be fetched again`() {
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(emptyList()))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success()))

    uiEvents.onNext(RegistrationFacilitySelectionRetryClicked())

    verify(screen).hideError()
    verify(screen).showProgressIndicator()
    verify(facilitySync).pullWithResult()
    verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when facilities are received then their UI models for facility list should be created`() {
    val facility1 = PatientMocker.facility(name = "Facility 1")
    val facility2 = PatientMocker.facility(name = "Facility 2")
    val facilities = listOf(facility1, facility2)
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities, facilities))

    uiEvents.onNext(ScreenCreated())

    verify(screen, times(2)).updateFacilities(listOf(facility1, facility2))
  }

  @Test
  fun `when a facility is clicked then the ongoing entry should be updated with selected facility and the user should be logged in`() {
    val ongoingEntry = OngoingRegistrationEntry(
        uuid = UUID.randomUUID(),
        phoneNumber = "1234567890",
        fullName = "Ashok",
        pin = "1234",
        pinConfirmation = "5678",
        createdAt = Instant.now())
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.loginFromOngoingRegistrationEntry()).thenReturn(Completable.complete())
    whenever(registrationScheduler.schedule()).thenReturn(Completable.complete())

    val facility1 = PatientMocker.facility(name = "Hoshiarpur", uuid = UUID.randomUUID())
    uiEvents.onNext(RegistrationFacilityClicked(facility1))

    val inOrder = inOrder(userSession, registrationScheduler, screen)
    inOrder.verify(userSession).loginFromOngoingRegistrationEntry()
    inOrder.verify(registrationScheduler).schedule()
    inOrder.verify(screen).openHomeScreen()
    verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.copy(facilityIds = listOf(facility1.uuid)))
  }
}
