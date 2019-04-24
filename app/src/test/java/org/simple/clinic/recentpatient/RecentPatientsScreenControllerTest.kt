package org.simple.clinic.recentpatient

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

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
        utcClock = UtcClock()
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
    whenever(patientRepository.recentPatients(facility.uuid)).thenReturn(Observable.just(listOf(
        PatientMocker.recentPatient(
            uuid = patientUuid1,
            fullName = "Ajay Kumar",
            age = Age(42, Instant.now(), LocalDate.MIN),
            gender = Gender.TRANSGENDER,
            lastBp = RecentPatient.LastBp(systolic = 127, diastolic = 83, createdAt = Instant.now())
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid2,
            fullName = "Vijay Kumar",
            age = Age(24, Instant.now(), LocalDate.MIN),
            gender = Gender.MALE,
            lastBp = null
        ),
        PatientMocker.recentPatient(
            uuid = patientUuid3,
            fullName = "Vinaya Kumari",
            age = Age(27, Instant.now(), LocalDate.MIN),
            gender = Gender.FEMALE,
            lastBp = RecentPatient.LastBp(systolic = 142, diastolic = 72, createdAt = Instant.now().minus(1, ChronoUnit.DAYS))
        )
    )))

    uiEvents.onNext(ScreenCreated())

    verify(screen).updateRecentPatients(listOf(
        RecentPatientItem(
            uuid = patientUuid1,
            name = "Ajay Kumar",
            age = 42,
            lastBp = RecentPatientItem.LastBp(
                systolic = 127,
                diastolic = 83,
                updatedAtRelativeTimestamp = Today
            ),
            gender = Gender.TRANSGENDER
        ),
        RecentPatientItem(
            uuid = patientUuid2,
            name = "Vijay Kumar",
            age = 24,
            lastBp = null,
            gender = Gender.MALE
        ),
        RecentPatientItem(
            uuid = patientUuid3,
            name = "Vinaya Kumari",
            age = 27,
            lastBp = RecentPatientItem.LastBp(
                systolic = 142,
                diastolic = 72,
                updatedAtRelativeTimestamp = Yesterday
            ),
            gender = Gender.FEMALE
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

