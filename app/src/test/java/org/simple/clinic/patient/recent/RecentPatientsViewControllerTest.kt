package org.simple.clinic.patient.recent

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
import org.simple.clinic.patient.Gender.FEMALE
import org.simple.clinic.patient.Gender.MALE
import org.simple.clinic.patient.Gender.TRANSGENDER
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.recent.RecentPatient.LastBp
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
  private val facilityUuid = UUID.randomUUID()
  private val relativeTimestampGenerator = RelativeTimestampGenerator()
  private val recentPatientLimit = 10

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
    whenever(facilityRepository.currentFacilityUuid(loggedInUser)).thenReturn(facilityUuid)
  }

  @Test
  fun `when screen opens then fetch and set recent patients`() {
    whenever(patientRepository.recentPatients(facilityUuid, recentPatientLimit)).thenReturn(Observable.just(listOf(
        PatientMocker.recentPatient(
            fullName = "Ajay Kumar",
            age = Age(42, Instant.now(), LocalDate.MIN),
            gender = TRANSGENDER,
            lastBp = LastBp(systolic = 127, diastolic = 83, updatedAt = Instant.now())
        ),
        PatientMocker.recentPatient(
            fullName = "Vijay Kumar",
            age = Age(24, Instant.now(), LocalDate.MIN),
            gender = MALE,
            lastBp = null
        ),
        PatientMocker.recentPatient(
            fullName = "Vinaya Kumari",
            age = Age(27, Instant.now(), LocalDate.MIN),
            gender = FEMALE,
            lastBp = LastBp(systolic = 142, diastolic = 72, updatedAt = Instant.now().minus(1, ChronoUnit.DAYS))
        )
    )))

    uiEvents.onNext(ScreenCreated())

    verify(screen).updateRecentPatients(listOf(
        RecentPatientItem(
            name = "Ajay Kumar",
            age = 42,
            lastBp = RecentPatientItem.LastBp(
                systolic = 127,
                diastolic = 83,
                updatedAtRelativeTimestamp = Today
            ),
            gender = TRANSGENDER
        ),
        RecentPatientItem(
            name = "Vijay Kumar",
            age = 24,
            lastBp = null,
            gender = MALE
        ),
        RecentPatientItem(
            name = "Vinaya Kumari",
            age = 27,
            lastBp = RecentPatientItem.LastBp(
                systolic = 142,
                diastolic = 72,
                updatedAtRelativeTimestamp = Yesterday
            ),
            gender = FEMALE
        )
    ))
    verify(screen).showNoRecentPatients(isVisible = false)
  }

  @Test
  fun `when screen opens and there are no recent patients then show "no recent patients view"`() {
    whenever(patientRepository.recentPatients(facilityUuid, recentPatientLimit)).thenReturn(Observable.just(emptyList()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).updateRecentPatients(emptyList())
    verify(screen).showNoRecentPatients(isVisible = true)
  }
}
