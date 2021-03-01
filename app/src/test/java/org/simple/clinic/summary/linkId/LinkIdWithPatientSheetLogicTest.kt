package org.simple.clinic.summary.linkId

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.spotify.mobius.Init
import dagger.Lazy
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.first
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class LinkIdWithPatientSheetLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<LinkIdWithPatientViewUi>()
  private val uiActions = mock<LinkIdWithPatientUiActions>()
  private val patientRepository = mock<PatientRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val patientUuid = UUID.fromString("755bfa1a-afa5-4c80-9ec7-57d81dff2ca1")
  private val patientName = "TestName"
  private val patient = TestData.patient(uuid = patientUuid, fullName = patientName)
  private val identifierUuid = UUID.fromString("097a39e5-945f-44de-8293-f75960c0a54e")
  private val identifier = Identifier(
      value = "40269f4d-f177-44a5-9db7-3cb8a7a53b33",
      type = Identifier.IdentifierType.BpPassport
  )

  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("5039c37f-3752-4dcb-ad69-0b6e38e02107")
  )

  private lateinit var testFixture: MobiusTestFixture<LinkIdWithPatientModel, LinkIdWithPatientEvent, LinkIdWithPatientEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when add is clicked, id should be added to patient and sheet should close`() {
    // given
    val businessId = TestData.businessId(uuid = identifierUuid, patientUuid = patientUuid, identifier = identifier)

    whenever(patientRepository.patientImmediate(patientUuid)).thenReturn(patient)
    whenever(patientRepository.addIdentifierToPatient(
        uuid = identifierUuid,
        patientUuid = patientUuid,
        identifier = identifier,
        assigningUser = user
    )).thenReturn(Single.just(businessId))

    // when
    setupController()
    uiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
    uiEvents.onNext(LinkIdWithPatientAddClicked)

    // then
    verify(patientRepository).addIdentifierToPatient(
        uuid = identifierUuid,
        patientUuid = patientUuid,
        identifier = identifier,
        assigningUser = user
    )

    verify(ui, times(3)).renderPatientName(patientName)
    verify(ui).showAddButtonProgress()
    verify(ui, times(4)).hideAddButtonProgress()
    verify(uiActions).closeSheetWithIdLinked()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when cancel is clicked, the sheet should close without saving id`() {
    // when
    whenever(patientRepository.patientImmediate(patientUuid)).thenReturn(patient)
    setupController()
    uiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
    uiEvents.onNext(LinkIdWithPatientCancelClicked)

    // then
    verify(ui).renderPatientName(patientName)
    verify(ui, times(3)).hideAddButtonProgress()
    verify(uiActions).closeSheetWithoutIdLinked()
    verifyNoMoreInteractions(ui, uiActions)

    verify(patientRepository, never()).addIdentifierToPatient(any(), any(), any(), any())
  }

  private fun setupController() {
    val uuidGenerator = FakeUuidGenerator.fixed(identifierUuid)

    val effectHandler = LinkIdWithPatientEffectHandler(
        currentUser = Lazy { user },
        patientRepository = patientRepository,
        uuidGenerator = uuidGenerator,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )

    val uiRenderer = LinkIdWithPatientUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = LinkIdWithPatientModel.create(),
        init = Init { first(it) },
        update = LinkIdWithPatientUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
