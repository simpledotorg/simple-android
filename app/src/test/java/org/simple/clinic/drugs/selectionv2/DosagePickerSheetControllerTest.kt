package org.simple.clinic.drugs.selectionv2

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.drugs.selectionv2.dosage.DosageOption
import org.simple.clinic.drugs.selectionv2.dosage.DosageListItem
import org.simple.clinic.drugs.selectionv2.dosage.DosagePickerSheet
import org.simple.clinic.drugs.selectionv2.dosage.DosagePickerSheetController
import org.simple.clinic.drugs.selectionv2.dosage.DosagePickerSheetCreated
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class DosagePickerSheetControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val protocolRepository = mock<ProtocolRepository>()
  private val userSession = mock<UserSession>()
  private val screen = mock<DosagePickerSheet>()
  private val facilityRepository = mock<FacilityRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: DosagePickerSheetController

  @Before
  fun setUp() {
    controller = DosagePickerSheetController(userSession, facilityRepository, protocolRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when sheet is created, list of dosages for that drug should be displayed`() {
    val protocolUuid = UUID.randomUUID()
    val currentFacility = PatientMocker.facility(protocolUuid = protocolUuid)
    val drugName = "Amlodipine"

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(PatientMocker.loggedInUser()))
    whenever(facilityRepository.currentFacility(any<User>())).thenReturn(Observable.just(currentFacility))
    whenever(protocolRepository.dosagesForDrugOrDefault(drugName, protocolUuid)).thenReturn(Observable.just(listOf("5 mg", "10 mg")))

    uiEvents.onNext(DosagePickerSheetCreated(drugName))

    verify(screen).populateDosageList(listOf(
        DosageListItem(DosageOption.Dosage("5 mg")),
        DosageListItem(DosageOption.Dosage("10 mg")),
        DosageListItem(DosageOption.None)
    ))
  }
}
