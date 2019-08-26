package org.simple.clinic.recentpatientsview

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import junitparams.JUnitParamsRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.Today
import org.simple.clinic.util.WithinSixMonths
import org.simple.clinic.util.Yesterday
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class RecentPatientsViewControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: RecentPatientsView = mock()
  private val userSession: UserSession = mock()
  private val patientRepository: PatientRepository = mock()
  private val facilityRepository: FacilityRepository = mock()

  private val uiEvents: Subject<UiEvent> = PublishSubject.create()
  private val loggedInUser = PatientMocker.loggedInUser()
  private val facility = PatientMocker.facility()
  private val relativeTimestampGenerator = RelativeTimestampGenerator()
  private val recentPatientLimit = 3
  private val recentPatientLimitPlusOne = recentPatientLimit + 1

  @Before
  fun setUp() {
    // This is needed because we manually subscribe to the refresh user status
    // operation on the IO thread, which was causing flakiness in this test.
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    val controller = RecentPatientsViewController(
        userSession = userSession,
        patientRepository = patientRepository,
        facilityRepository = facilityRepository,
        relativeTimestampGenerator = relativeTimestampGenerator,
        utcClock = UtcClock(),
        patientConfig = Observable.just(PatientConfig(
            limitOfSearchResults = 1,
            scanSimpleCardFeatureEnabled = false,
            recentPatientLimit = recentPatientLimit
        ))
    )

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(loggedInUser))
    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility))
  }

  @Test
  fun `when screen opens then fetch and set recent patients`() {
    val patientUuid1 = UUID.randomUUID()
    val patientUuid2 = UUID.randomUUID()
    val patientUuid3 = UUID.randomUUID()
    whenever(patientRepository.recentPatients(
        facilityUuid = facility.uuid,
        limit = recentPatientLimitPlusOne
    )).thenReturn(Observable.just(listOf(
        PatientMocker.recentPatient(
            uuid = patientUuid1,
            fullName = "Ajay Kumar",
            age = Age(42, Instant.now(), LocalDate.MIN),
            gender = Transgender
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid2,
            fullName = "Vijay Kumar",
            age = Age(24, Instant.now(), LocalDate.MIN),
            gender = Male,
            updatedAt = Instant.now().minus(1, ChronoUnit.DAYS)
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid3,
            fullName = "Vinaya Kumari",
            age = Age(27, Instant.now(), LocalDate.MIN),
            gender = Female,
            updatedAt = Instant.now().minus(3, ChronoUnit.DAYS)
        )
    )))

    uiEvents.onNext(ScreenCreated())

    verify(screen).updateRecentPatients(listOf(
        RecentPatientItem(
            uuid = patientUuid1,
            name = "Ajay Kumar",
            age = 42,
            gender = Transgender,
            updatedAt = Today
        ),
        RecentPatientItem(
            uuid = patientUuid2,
            name = "Vijay Kumar",
            age = 24,
            gender = Male,
            updatedAt = Yesterday
        ),
        RecentPatientItem(
            uuid = patientUuid3,
            name = "Vinaya Kumari",
            age = 27,
            gender = Female,
            updatedAt = WithinSixMonths(3)
        )
    ))
    verify(screen).showOrHideRecentPatients(isVisible = true)
  }

  @Test
  fun `when number of recent patients is greater than recent patient limit then add see all item`() {
    val patientUuid1 = UUID.randomUUID()
    val patientUuid2 = UUID.randomUUID()
    val patientUuid3 = UUID.randomUUID()
    val patientUuid4 = UUID.randomUUID()
    whenever(patientRepository.recentPatients(
        facilityUuid = facility.uuid,
        limit = recentPatientLimitPlusOne
    )).thenReturn(Observable.just(listOf(
        PatientMocker.recentPatient(
            uuid = patientUuid1,
            fullName = "Ajay Kumar",
            age = Age(42, Instant.now(), LocalDate.MIN),
            gender = Transgender
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid2,
            fullName = "Vijay Kumar",
            age = Age(24, Instant.now(), LocalDate.MIN),
            gender = Male,
            updatedAt = Instant.now().minus(1, ChronoUnit.DAYS)
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid3,
            fullName = "Vinaya Kumari",
            age = Age(27, Instant.now(), LocalDate.MIN),
            gender = Female,
            updatedAt = Instant.now().minus(4, ChronoUnit.DAYS)
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid4,
            fullName = "Abhilash Devi",
            age = Age(37, Instant.now(), LocalDate.MIN),
            gender = Transgender
        )
    )))

    uiEvents.onNext(ScreenCreated())

    verify(screen).updateRecentPatients(listOf(
        RecentPatientItem(
            uuid = patientUuid1,
            name = "Ajay Kumar",
            age = 42,
            gender = Transgender,
            updatedAt = Today
        ),
        RecentPatientItem(
            uuid = patientUuid2,
            name = "Vijay Kumar",
            age = 24,
            gender = Male,
            updatedAt = Yesterday
        ),
        RecentPatientItem(
            uuid = patientUuid3,
            name = "Vinaya Kumari",
            age = 27,
            gender = Female,
            updatedAt = WithinSixMonths(4)
        ),
        SeeAllItem
    ))
    verify(screen).showOrHideRecentPatients(isVisible = true)
  }

  @Test
  fun `when screen opens and there are no recent patients then show empty state`() {
    whenever(patientRepository.recentPatients(
        facilityUuid = facility.uuid,
        limit = recentPatientLimitPlusOne
    )).thenReturn(Observable.just(emptyList()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showOrHideRecentPatients(isVisible = false)
  }

  @Test
  fun `when any recent patient item is clicked, then open patient summary`() {
    val patientUuid = UUID.randomUUID()
    uiEvents.onNext(RecentPatientItemClicked(patientUuid = patientUuid))

    verify(screen).openPatientSummary(patientUuid)
  }

  @Test
  fun `when see all is clicked, then open recent patients screen`() {
    uiEvents.onNext(SeeAllItemClicked)

    verify(screen).openRecentPatientsScreen()
  }
}
