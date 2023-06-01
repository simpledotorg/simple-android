package org.simple.clinic.registration.facility

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.sharedTestCode.util.RxErrorsRule
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
    val facility1 = TestData.facility(name = "Hoshiarpur", uuid = UUID.fromString("5cf9d744-7f34-4633-aa46-a6c7e7542060"))

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
      viewEffectsConsumer = RegistrationFacilitySelectionViewEffectHandler(uiActions)::handle
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = RegistrationFacilitySelectionModel.create(ongoingRegistrationEntry),
        init = RegistrationFacilitySelectionInit(),
        update = RegistrationFacilitySelectionUpdate(true),
        effectHandler = effectHandler.build(),
        modelUpdateListener = { /* Nothing to do here */ }
    )
    testFixture.start()
  }
}
