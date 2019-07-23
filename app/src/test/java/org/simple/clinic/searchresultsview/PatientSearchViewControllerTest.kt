package org.simple.clinic.searchresultsview

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.PatientToFacilityId
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.searchresultsview.SearchResultsItemType.InCurrentFacilityHeader
import org.simple.clinic.searchresultsview.SearchResultsItemType.NotInCurrentFacilityHeader
import org.simple.clinic.searchresultsview.SearchResultsItemType.SearchResultRow
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSearchViewControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<PatientSearchView>()
  private val patientRepository = mock<PatientRepository>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val bloodPressureDao = mock<BloodPressureMeasurement.RoomDao>()

  private val controller = PatientSearchViewController(
      patientRepository = patientRepository,
      userSession = userSession,
      facilityRepository = facilityRepository,
      bloodPressureDao = bloodPressureDao
  )
  private val uiEvents = PublishSubject.create<UiEvent>()

  private val currentFacility = PatientMocker.facility(UUID.fromString("69cf85c8-6788-4071-b985-0536ae606b70"))
  private val otherFacility = PatientMocker.facility(UUID.fromString("0bb48f0a-3a6c-4e35-8781-74b074443f36"))
  private val user = PatientMocker.loggedInUser()

  private val patientName = "name"
  private val phoneNumber: String = "123456"

  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(currentFacility))
    whenever(patientRepository.search(Name(patientName))).thenReturn(Observable.never())
    whenever(patientRepository.search(PhoneNumber(phoneNumber))).thenReturn(Observable.never())
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  @Parameters(method = "params for search criteria")
  fun `when searching patients by name returns results, the results should be displayed`(
      searchCriteria: PatientSearchCriteria,
      searchPatientInput: SearchPatientInput
  ) {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = PatientMocker.patientSearchResult(patientUuid1)
    val patientSearchResult2 = PatientMocker.patientSearchResult(patientUuid2)
    whenever(bloodPressureDao.patientToFacilityIds(listOf(patientUuid1, patientUuid2)))
        .thenReturn(Flowable.just(listOf(
            PatientToFacilityId(patientUuid = patientUuid1, facilityUuid = currentFacility.uuid),
            PatientToFacilityId(patientUuid = patientUuid2, facilityUuid = otherFacility.uuid)
        )))
    whenever(patientRepository.search(searchCriteria))
        .thenReturn(Observable.just(listOf(patientSearchResult1, patientSearchResult2)))

    // when
    uiEvents.onNext(SearchResultsViewCreated)
    uiEvents.onNext(SearchPatientWithInput(searchPatientInput = searchPatientInput))

    // then
    verify(screen).updateSearchResults(listOf(
        InCurrentFacilityHeader(facilityName = currentFacility.name),
        SearchResultRow(
            searchResult = patientSearchResult1,
            currentFacility = currentFacility
        ),
        NotInCurrentFacilityHeader,
        SearchResultRow(
            searchResult = patientSearchResult2,
            currentFacility = currentFacility
        )
    ))
    verify(screen).setEmptyStateVisible(false)
  }

  @Test
  @Parameters(method = "params for search criteria")
  fun `when searching patients by name returns no results, the empty state should be displayed`(
      searchCriteria: PatientSearchCriteria,
      searchPatientInput: SearchPatientInput
  ) {
    // given
    whenever(patientRepository.search(searchCriteria))
        .thenReturn(Observable.just(emptyList()))
    whenever(bloodPressureDao.patientToFacilityIds(emptyList()))
        .thenReturn(Flowable.just(emptyList()))

    // when
    uiEvents.onNext(SearchResultsViewCreated)
    uiEvents.onNext(SearchPatientWithInput(searchPatientInput = searchPatientInput))

    // then
    verify(screen).updateSearchResults(emptyList())
    verify(screen).setEmptyStateVisible(true)
  }

  @Test
  @Parameters(method = "params for search criteria")
  fun `when searching by name and there are patients only in current facility, then "Other Results" header should not be shown`(
      searchCriteria: PatientSearchCriteria,
      searchPatientInput: SearchPatientInput
  ) {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = PatientMocker.patientSearchResult(patientUuid1)
    val patientSearchResult2 = PatientMocker.patientSearchResult(patientUuid2)
    whenever(bloodPressureDao.patientToFacilityIds(listOf(patientUuid1, patientUuid2)))
        .thenReturn(Flowable.just(listOf(
            PatientToFacilityId(patientUuid = patientUuid1, facilityUuid = currentFacility.uuid),
            PatientToFacilityId(patientUuid = patientUuid2, facilityUuid = currentFacility.uuid)
        )))
    whenever(patientRepository.search(searchCriteria))
        .thenReturn(Observable.just(listOf(patientSearchResult1, patientSearchResult2)))

    // when
    uiEvents.onNext(SearchResultsViewCreated)
    uiEvents.onNext(SearchPatientWithInput(searchPatientInput = searchPatientInput))

    // then
    verify(screen).updateSearchResults(listOf(
        InCurrentFacilityHeader(facilityName = currentFacility.name),
        SearchResultRow(
            searchResult = patientSearchResult1,
            currentFacility = currentFacility
        ),
        SearchResultRow(
            searchResult = patientSearchResult2,
            currentFacility = currentFacility
        )
    ))
    verify(screen).setEmptyStateVisible(false)
  }

  @Test
  @Parameters(method = "params for search criteria")
  fun `when searching by name and there are patients only in other facilities, then current facility header with "no results" should be shown`(
      searchCriteria: PatientSearchCriteria,
      searchPatientInput: SearchPatientInput
  ) {
    // given
    val patientUuid1 = UUID.fromString("1d5f18d9-43f7-4e7f-92d3-a4f641709470")
    val patientUuid2 = UUID.fromString("139bfac5-1adc-43fa-9406-d1000fb67a88")
    val patientSearchResult1 = PatientMocker.patientSearchResult(patientUuid1)
    val patientSearchResult2 = PatientMocker.patientSearchResult(patientUuid2)
    whenever(bloodPressureDao.patientToFacilityIds(listOf(patientUuid1, patientUuid2)))
        .thenReturn(Flowable.just(listOf(
            PatientToFacilityId(patientUuid = patientUuid1, facilityUuid = otherFacility.uuid),
            PatientToFacilityId(patientUuid = patientUuid2, facilityUuid = otherFacility.uuid)
        )))
    whenever(patientRepository.search(searchCriteria))
        .thenReturn(Observable.just(listOf(patientSearchResult1, patientSearchResult2)))

    // then
    uiEvents.onNext(SearchResultsViewCreated)
    uiEvents.onNext(SearchPatientWithInput(searchPatientInput = searchPatientInput))

    // then
    verify(screen).updateSearchResults(listOf(
        InCurrentFacilityHeader(facilityName = currentFacility.name),
        SearchResultsItemType.NoPatientsInCurrentFacility,
        NotInCurrentFacilityHeader,
        SearchResultRow(
            searchResult = patientSearchResult1,
            currentFacility = currentFacility
        ),
        SearchResultRow(
            searchResult = patientSearchResult2,
            currentFacility = currentFacility
        )
    ))
    verify(screen).setEmptyStateVisible(false)
  }

  @Suppress("Unused")
  private fun `params for search criteria`(): List<List<Any>> {
    fun testCase(
        searchCriteria: PatientSearchCriteria,
        searchPatientInput: SearchPatientInput
    ): List<Any> {
      return listOf(searchCriteria, searchPatientInput)
    }

    return listOf(
        testCase(
            searchCriteria = Name(patientName = patientName),
            searchPatientInput = SearchPatientInput.Name(searchText = patientName)
        ),
        testCase(
            searchCriteria = PhoneNumber(phoneNumber = phoneNumber),
            searchPatientInput = SearchPatientInput.PhoneNumber(searchText = phoneNumber)
        )
    )
  }

  @Test
  fun `when search result clicked then SearchResultClicked event should be emitted`() {
    val searchResult = PatientMocker.patientSearchResult()
    val searchResultClicked = SearchResultClicked(searchResult)
    uiEvents.onNext(searchResultClicked)

    verify(screen).searchResultClicked(searchResultClicked)
  }

  @Test
  @Parameters(method = "params for search by parameters on register click")
  fun `when register new patient clicked then RegisterNewPatient event should be emitted`(searchPatientInput: SearchPatientInput) {
    uiEvents.onNext(SearchResultsViewCreated)
    uiEvents.onNext(SearchPatientWithInput(searchPatientInput))
    uiEvents.onNext(RegisterNewPatientClicked)

    verify(screen).registerNewPatient(RegisterNewPatient(searchPatientInput))
  }

  @Suppress("Unused")
  private fun `params for search by parameters on register click`(): List<SearchPatientInput> {
    return listOf(
        SearchPatientInput.Name(searchText = patientName),
        SearchPatientInput.PhoneNumber(searchText = phoneNumber)
    )
  }
}
