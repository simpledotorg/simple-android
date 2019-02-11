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
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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

@RunWith(JUnitParamsRunner::class)
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
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
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

    val facilityListItems = FacilityListItemBuilder.build(facilities, searchQuery)

    verify(screen).updateFacilities(facilityListItems, FIRST_UPDATE)
    verify(screen).updateFacilities(facilityListItems, SUBSEQUENT_UPDATE)
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
  @Parameters(method = "params for when a facility is changed then the report has to be deleted and synced")
  fun `when a facility is changed then the report has to be deleted and synced`(
      deleteReport: Single<DeleteFileResult>,
      reportsSyncCompletable: Completable
  ) {
    val newFacility = PatientMocker.facility()
    whenever(facilityRepository.associateUserWithFacility(user, newFacility)).thenReturn(Completable.complete())
    whenever(facilityRepository.setCurrentFacility(user, newFacility)).thenReturn(Completable.complete())
    whenever(reportsRepository.deleteReportsFile()).thenReturn(deleteReport)
    whenever(reportsSync.sync()).thenReturn(reportsSyncCompletable)

    uiEvents.onNext(FacilityChangeClicked(newFacility))

    deleteReport.test().assertSubscribed()
    reportsSyncCompletable.test().assertSubscribed()
    verify(screen).goBack()
  }

  @Suppress("Unused")
  private fun `params for when a facility is changed then the report has to be deleted and synced`(): List<List<Any>> =
      listOf(
          listOf(Single.just(DeleteFileResult.Success), Completable.complete()),
          listOf(Single.just(DeleteFileResult.Success), Completable.error(Exception())),
          listOf(Single.just(DeleteFileResult.Failure(Exception())), Completable.complete()),
          listOf(Single.just(DeleteFileResult.Failure(Exception())), Completable.error(Exception()))
      )
}
