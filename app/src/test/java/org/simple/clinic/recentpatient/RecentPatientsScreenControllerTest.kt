package org.simple.clinic.recentpatient

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
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.summary.RelativeTimestampGenerator
import org.simple.clinic.summary.Today
import org.simple.clinic.summary.Yesterday
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class RecentPatientsScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: RecentPatientsScreen = mock()
  private val userSession: UserSession = mock()
  private val patientRepository: PatientRepository = mock()
  private val facilityRepository: FacilityRepository = mock()

  private val uiEvents: Subject<UiEvent> = PublishSubject.create()
  private val loggedInUser = PatientMocker.loggedInUser()
  private val facility = PatientMocker.facility()
  private val relativeTimestampGenerator = RelativeTimestampGenerator()
  private val dateFormatter: DateTimeFormatter = mock()

  @Before
  fun setUp() {
    // This is needed because we manually subscribe to the refresh user status
    // operation on the IO thread, which was causing flakiness in this test.
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    val controller = RecentPatientsScreenController(
        userSession = userSession,
        patientRepository = patientRepository,
        facilityRepository = facilityRepository,
        relativeTimestampGenerator = relativeTimestampGenerator,
        recentPatientRelativeTimestampGenerator = RecentPatientRelativeTimeStampGenerator(ZoneOffset.UTC),
        utcClock = UtcClock(),
        userClock = TestUserClock(),
        dateFormatter = dateFormatter
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

    val today = Instant.now()
    val yesterday = today.minus(Duration.ofDays(1))
    val twoDaysAgo = yesterday.minus(Duration.ofDays(2))
    whenever(patientRepository.recentPatients(facility.uuid)).thenReturn(Observable.just(listOf(
        PatientMocker.recentPatient(
            uuid = patientUuid1,
            fullName = "Ajay Kumar",
            age = Age(42, Instant.now(), LocalDate.MIN),
            gender = Gender.TRANSGENDER,
            lastBp = RecentPatient.LastBp(systolic = 127, diastolic = 83, recordedAt = Instant.now()),
            updatedAt = today
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid2,
            fullName = "Vijay Kumar",
            age = Age(24, Instant.now(), LocalDate.MIN),
            gender = Gender.MALE,
            lastBp = null,
            updatedAt = yesterday
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid3,
            fullName = "Vinaya Kumari",
            age = Age(27, Instant.now(), LocalDate.MIN),
            gender = Gender.FEMALE,
            lastBp = RecentPatient.LastBp(systolic = 142, diastolic = 72, recordedAt = Instant.now().minus(1, ChronoUnit.DAYS)),
            updatedAt = twoDaysAgo
        )
    )))

    uiEvents.onNext(ScreenCreated())

    verify(screen).updateRecentPatients(listOf(
        DateHeader(RelativeTimestamp.Today, dateFormatter),
        RecentPatientItem(
            uuid = patientUuid1,
            name = "Ajay Kumar",
            age = 42,
            lastBp = RecentPatientItem.LastBp(
                systolic = 127,
                diastolic = 83,
                updatedAtRelativeTimestamp = Today
            ),
            gender = Gender.TRANSGENDER,
            updatedAt = today
        ),
        DateHeader(RelativeTimestamp.Yesterday, dateFormatter),
        RecentPatientItem(
            uuid = patientUuid2,
            name = "Vijay Kumar",
            age = 24,
            lastBp = null,
            gender = Gender.MALE,
            updatedAt = yesterday
        ),
        DateHeader(RelativeTimestamp.OlderThanTwoDays(twoDaysAgo.toLocalDateAtZone(ZoneOffset.UTC)), dateFormatter),
        RecentPatientItem(
            uuid = patientUuid3,
            name = "Vinaya Kumari",
            age = 27,
            lastBp = RecentPatientItem.LastBp(
                systolic = 142,
                diastolic = 72,
                updatedAtRelativeTimestamp = Yesterday
            ),
            gender = Gender.FEMALE,
            updatedAt = twoDaysAgo
        )
    ))
  }

  @Test
  fun `when any recent patient item is clicked, then open patient summary`() {
    val patientUuid = UUID.randomUUID()
    uiEvents.onNext(RecentPatientItemClicked(patientUuid = patientUuid))

    verify(screen).openPatientSummary(patientUuid)
  }
}

