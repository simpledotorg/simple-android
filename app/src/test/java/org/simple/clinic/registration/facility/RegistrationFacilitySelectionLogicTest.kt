package org.simple.clinic.registration.facility

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Instant
import java.util.UUID

class RegistrationFacilitySelectionLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val uiActions = mock<RegistrationFacilitySelectionUiActions>()
  private val userSession = mock<UserSession>()
  private val currentTime = Instant.parse("2018-01-01T00:00:00Z")
  private val ongoingEntry = OngoingRegistrationEntry(
      uuid = UUID.fromString("759f5f53-6f71-4a00-825b-c74654a5e448"),
      phoneNumber = "1111111111",
      fullName = "Anish Acharya",
      pin = "1234"
  )

  private lateinit var testFixture: MobiusTestFixture<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEvent, RegistrationFacilitySelectionEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when a facility is clicked then show confirm facility sheet`() {
    // given
    val ongoingEntry = OngoingRegistrationEntry(
        uuid = UUID.fromString("eb0a9bc0-b24d-4f3f-9990-aa05e217be1a"),
        phoneNumber = "1234567890",
        fullName = "Ashok",
        pin = "1234")
    val facility1 = TestData.facility(name = "Hoshiarpur", uuid = UUID.fromString("5cf9d744-7f34-4633-aa46-a6c7e7542060"))

    whenever(userSession.saveOngoingRegistrationEntryAsUser(ongoingEntry, currentTime)).thenReturn(Completable.complete())

    // when
    setupController()
    uiEvents.onNext(RegistrationFacilityClicked(facility1))

    // then
    verify(uiActions).showConfirmFacilitySheet(facility1.uuid, facility1.name)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when a facility is confirmed then open the intro video screen`() {
    // given
    val facility1 = TestData.facility(name = "Hoshiarpur", uuid = UUID.fromString("bc761c6c-032f-4f1d-a66a-3ec81e9e8aa3"))
    val entryWithFacility = ongoingEntry.copy(facilityId = facility1.uuid)

    // when
    setupController()
    uiEvents.onNext(RegistrationFacilityConfirmed(facility1.uuid))

    // then
    verify(uiActions).openIntroVideoScreen(entryWithFacility)
    verifyNoMoreInteractions(uiActions)
  }

  private fun setupController(
      ongoingRegistrationEntry: OngoingRegistrationEntry = ongoingEntry
  ) {
    val effectHandler = RegistrationFacilitySelectionEffectHandler(
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationFacilitySelectionModel.create(ongoingRegistrationEntry),
        init = RegistrationFacilitySelectionInit(),
        update = RegistrationFacilitySelectionUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = { /* Nothing to do here */ }
    )
    testFixture.start()
  }
}
