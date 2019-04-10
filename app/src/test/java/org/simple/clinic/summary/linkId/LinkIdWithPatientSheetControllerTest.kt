package org.simple.clinic.summary.linkId

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class LinkIdWithPatientSheetControllerTest {


  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val patientRepository = mock<PatientRepository>()

  private val sheet = mock<LinkIdWithPatientSheet>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var controller: LinkIdWithPatientSheetController

  private val patientUuid = UUID.randomUUID()

  private lateinit var identifier: Identifier

  @Before
  fun setUp() {

    controller = LinkIdWithPatientSheetController(patientRepository)

    identifier = Identifier(
        value = patientUuid.toString(),
        type = Identifier.IdentifierType.random()
    )

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(sheet) }
  }

  @Test
  fun `when add is clicked, id should be added to patient and sheet should close`() {
    val businessId = PatientMocker.businessId(patientUuid = patientUuid, identifier = identifier)

    whenever(patientRepository.addIdentifierToPatient(patientUuid, identifier)).thenReturn(Single.just(businessId))

    uiEvents.onNext(LinkIdWithPatientSheetCreated(patientUuid, identifier))
    uiEvents.onNext(LinkIdWithPatientAddClicked)

    verify(patientRepository).addIdentifierToPatient(patientUuid, identifier)
    verify(sheet).closeSheet()
  }
}
