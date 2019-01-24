package org.simple.clinic.facility.change

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.reports.ReportsSync
import org.simple.clinic.storage.files.DeleteFileResult
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.io.File

class FacilityChangeScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<FacilityChangeScreen>()
  private val facilityRepository = mock<FacilityRepository>()
  private val reportsRepository = mock<ReportsRepository>()
  private val userSession = mock<UserSession>()
  private val reportsSync: ReportsSync = mock()

  private val user = PatientMocker.loggedInUser()

  private lateinit var controller: FacilityChangeScreenController

  @Before
  fun setUp() {
    controller = FacilityChangeScreenController(facilityRepository, reportsRepository, userSession, reportsSync)

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then facilities UI models should be created`() {
    val facility1 = PatientMocker.facility()
    val facility2 = PatientMocker.facility()
    val facilities = listOf(facility1, facility2)
    whenever(facilityRepository.facilitiesInCurrentGroup(user = user)).thenReturn(Observable.just(facilities, facilities))

    val searchQuery = ""
    uiEvents.onNext(FacilityChangeSearchQueryChanged(searchQuery))

    val facility1ListItem = FacilityListItem.Builder.build(facility1, searchQuery)
    val facility2ListItem = FacilityListItem.Builder.build(facility2, searchQuery)

    verify(screen).updateFacilities(listOf(facility1ListItem, facility2ListItem), FIRST_UPDATE)
    verify(screen).updateFacilities(listOf(facility1ListItem, facility2ListItem), SUBSEQUENT_UPDATE)
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), eq(user))).thenReturn(Observable.just(facilities))

    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "F"))
    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "Fa"))
    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "Fac"))

    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "F", user = user)
    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "Fa", user = user)
    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "Fac", user = user)
  }

  @Test
  fun `when a facility is selected then the user's facility should be changed and the screen should be closed`() {
    val newFacility = PatientMocker.facility()
    val user = PatientMocker.loggedInUser()
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(facilityRepository.associateUserWithFacility(user, newFacility)).thenReturn(Completable.complete())
    whenever(facilityRepository.setCurrentFacility(user, newFacility)).thenReturn(Completable.complete())
    whenever(reportsRepository.reportsFile()).thenReturn(Observable.just(File("").toOptional()))

    whenever(reportsRepository.deleteReportsFile()).thenReturn(Single.just(DeleteFileResult.Success))
    whenever(reportsSync.sync()).thenReturn(Completable.complete())

    uiEvents.onNext(FacilityChangeClicked(newFacility))

    val inOrder = inOrder(facilityRepository, screen)
    inOrder.verify(facilityRepository).associateUserWithFacility(user, newFacility)
    inOrder.verify(screen).goBack()
  }

  @Test
  fun `when a facility is changed then the report has to be deleted and synced`() {
    val newFacility = PatientMocker.facility()
    whenever(facilityRepository.associateUserWithFacility(user, newFacility)).thenReturn(Completable.complete())
    whenever(facilityRepository.setCurrentFacility(user, newFacility)).thenReturn(Completable.complete())

    val deleteReport: Single<DeleteFileResult> = Single.just(DeleteFileResult.Success)
    whenever(reportsRepository.deleteReportsFile()).thenReturn(deleteReport)

    val reportsSyncCompletable = Completable.complete()
    whenever(reportsSync.sync()).thenReturn(reportsSyncCompletable)

    uiEvents.onNext(FacilityChangeClicked(newFacility))

    deleteReport.test().assertSubscribed()
    reportsSyncCompletable.test().assertSubscribed()
  }
}
