package org.simple.clinic.summary.linkId

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class LinkIdWithPatientViewControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val patientRepository = mock<PatientRepository>()

  private val view = mock<LinkIdWithPatientView>()

  private val userSession = mock<UserSession>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val patientUuid = UUID.randomUUID()

  private val identifier = Identifier(
      value = patientUuid.toString(),
      type = Identifier.IdentifierType.random()
  )

  private val user = PatientMocker.loggedInUser()

  private val facility = PatientMocker.facility()

  private val facilityRepository = mock<FacilityRepository>()

  private val controller = LinkIdWithPatientViewController(patientRepository, userSession, facilityRepository)

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))
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
    val businessId = PatientMocker.businessId(patientUuid = patientUuid, identifier = identifier)

    whenever(patientRepository.addIdentifierToPatient(patientUuid, identifier, user, facility)).thenReturn(Single.just(businessId))

    uiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
    uiEvents.onNext(LinkIdWithPatientAddClicked)

    verify(patientRepository).addIdentifierToPatient(patientUuid, identifier, user, facility)
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
