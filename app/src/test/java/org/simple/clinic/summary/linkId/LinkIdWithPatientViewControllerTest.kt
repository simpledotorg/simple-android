package org.simple.clinic.summary.linkId

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class LinkIdWithPatientViewControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val patientRepository = mock<PatientRepository>()

  private val view = mock<LinkIdWithPatientView>()

  private val userSession = mock<UserSession>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val patientUuid = UUID.fromString("755bfa1a-afa5-4c80-9ec7-57d81dff2ca1")

  private val identifier = Identifier(
      value = "40269f4d-f177-44a5-9db7-3cb8a7a53b33",
      type = Identifier.IdentifierType.BpPassport
  )

  private val identifierUuid = UUID.fromString("097a39e5-945f-44de-8293-f75960c0a54e")

  private val user = TestData.loggedInUser()

  private val controller = LinkIdWithPatientViewController(
      patientRepository = patientRepository,
      userSession = userSession,
      uuidGenerator = FakeUuidGenerator.fixed(identifierUuid)
  )

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(view) }
  }

  @Test
  fun `when the view is created, the identifier must be displayed`() {
    uiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))

    verify(view).renderIdentifierText(identifier)
  }

  @Test
  fun `when add is clicked, id should be added to patient and sheet should close`() {
    val businessId = TestData.businessId(uuid = identifierUuid, patientUuid = patientUuid, identifier = identifier)

    whenever(patientRepository.addIdentifierToPatient(
        uuid = identifierUuid,
        patientUuid = patientUuid,
        identifier = identifier,
        assigningUser = user
    )).thenReturn(Single.just(businessId))

    uiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
    uiEvents.onNext(LinkIdWithPatientAddClicked)

    verify(patientRepository).addIdentifierToPatient(
        uuid = identifierUuid,
        patientUuid = patientUuid,
        identifier = identifier,
        assigningUser = user
    )
    verify(view).closeSheetWithIdLinked()
  }

  @Test
  fun `when cancel is clicked, the sheet should close without saving id`() {
    uiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
    uiEvents.onNext(LinkIdWithPatientCancelClicked)

    verify(view).closeSheetWithoutIdLinked()
    verify(patientRepository, never()).addIdentifierToPatient(any(), any(), any(), any())
  }
}
