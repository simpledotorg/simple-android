package org.simple.clinic.recentpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class RecentPatientsScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: RecentPatientsScreen = mock()
  private val userSession: UserSession = mock()
  private val patientRepository: PatientRepository = mock()
  private val facilityRepository: FacilityRepository = mock()

  private val uiEvents: Subject<UiEvent> = PublishSubject.create()
  private val loggedInUser = TestData.loggedInUser(uuid = UUID.fromString("d192d336-1831-439d-ab6c-2b9228e0a06a"))
  private val facility = TestData.facility(uuid = UUID.fromString("b8e0fe64-6c31-43df-837a-80ce05b80cea"))
  private val dateFormatter = DateTimeFormatter.ISO_INSTANT
  private val userClock = TestUserClock(LocalDate.parse("2020-02-14"))

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when screen opens then fetch and set recent patients`() {
    val patientUuid1 = UUID.fromString("b5cfb2e6-0d73-4189-8131-5b91d8c45780")
    val patientUuid2 = UUID.fromString("47f4281e-d571-4c36-a7fc-039d0a289e2f")
    val patientUuid3 = UUID.fromString("073c9099-5f60-4bc3-b40e-2c67f5ac74e4")

    val today = Instant.now(userClock)
    val yesterday = today.minus(Duration.ofDays(1))
    val twoDaysAgo = today.minus(Duration.ofDays(2))
    whenever(patientRepository.recentPatients(facility.uuid)).thenReturn(Observable.just(listOf(
        TestData.recentPatient(
            uuid = patientUuid1,
            fullName = "Ajay Kumar",
            age = Age(42, Instant.now(userClock)),
            gender = Gender.Transgender,
            updatedAt = today,
            patientRecordedAt = today
        ),
        TestData.recentPatient(
            uuid = patientUuid2,
            fullName = "Vijay Kumar",
            age = Age(24, Instant.now(userClock)),
            gender = Gender.Male,
            updatedAt = yesterday,
            patientRecordedAt = twoDaysAgo
        ),
        TestData.recentPatient(
            uuid = patientUuid3,
            fullName = "Vinaya Kumari",
            age = Age(27, Instant.now(userClock)),
            gender = Gender.Female,
            updatedAt = twoDaysAgo,
            patientRecordedAt = twoDaysAgo
        )
    )))

    setupController()

    verify(screen).updateRecentPatients(listOf(
        RecentPatientItem(
            uuid = patientUuid1,
            name = "Ajay Kumar",
            age = 42,
            gender = Gender.Transgender,
            lastSeen = Instant.parse("2020-02-14T00:00:00Z"),
            dateFormatter = dateFormatter,
            clock = userClock,
            isNewRegistration = true
        ),
        RecentPatientItem(
            uuid = patientUuid2,
            name = "Vijay Kumar",
            age = 24,
            gender = Gender.Male,
            lastSeen = Instant.parse("2020-02-13T00:00:00Z"),
            dateFormatter = dateFormatter,
            clock = userClock,
            isNewRegistration = false
        ),
        RecentPatientItem(
            uuid = patientUuid3,
            name = "Vinaya Kumari",
            age = 27,
            gender = Gender.Female,
            lastSeen = Instant.parse("2020-02-12T00:00:00Z"),
            dateFormatter = dateFormatter,
            clock = userClock,
            isNewRegistration = false
        )
    ))
  }

  @Test
  fun `when any recent patient item is clicked, then open patient summary`() {
    val patientUuid = UUID.fromString("c5070a89-d848-4822-80c2-d7c306e437b1")
    val today = Instant.now(userClock)

    whenever(patientRepository.recentPatients(facility.uuid)).thenReturn(Observable.just(listOf(
        TestData.recentPatient(
            uuid = patientUuid,
            fullName = "Ajay Kumar",
            age = Age(42, Instant.now(userClock)),
            gender = Gender.Transgender,
            updatedAt = today,
            patientRecordedAt = today
        )
    )))

    setupController()
    uiEvents.onNext(RecentPatientItemClicked(patientUuid = patientUuid))

    verify(screen).openPatientSummary(patientUuid)
  }

  private fun setupController() {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(loggedInUser.toOptional()))
    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility))

    val controller = RecentPatientsScreenController(
        userSession = userSession,
        patientRepository = patientRepository,
        facilityRepository = facilityRepository,
        userClock = userClock,
        dateFormatter = dateFormatter
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(ScreenCreated())
  }
}

