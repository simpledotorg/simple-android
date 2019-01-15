package org.simple.clinic.drugs.selectionv2

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.selectionv2.dosage.DosageItemClicked
import org.simple.clinic.drugs.selectionv2.dosage.DosageSelected
import org.simple.clinic.drugs.selectionv2.dosage.DosageType
import org.simple.clinic.drugs.selectionv2.dosage.PrescribedDosageListItem
import org.simple.clinic.drugs.selectionv2.dosage.PrescribedDrugWithDosagesSheet
import org.simple.clinic.drugs.selectionv2.dosage.PrescribedDrugWithDosagesSheetController
import org.simple.clinic.drugs.selectionv2.dosage.PrescribedDrugsWithDosagesSheetCreated
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class PrescribedDrugWithDosagesSheetControllerTest {

  private val protocolRepository = mock<ProtocolRepository>()
  private val userSession = mock<UserSession>()
  private val screen = mock<PrescribedDrugWithDosagesSheet>()
  private val facilityRepository = mock<FacilityRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: PrescribedDrugWithDosagesSheetController

  @Before
  fun setUp() {
    controller = PrescribedDrugWithDosagesSheetController(userSession, facilityRepository, protocolRepository, prescriptionRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when sheet is created, list of dosages for that drug should be displayed`() {
    val protocolUuid = UUID.randomUUID()
    val currentFacility = PatientMocker.facility(protocolUuid = protocolUuid)
    val patientUUID = UUID.randomUUID()
    val drugName = "Amlodipine"

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(PatientMocker.loggedInUser()))
    whenever(facilityRepository.currentFacility(any<User>())).thenReturn(Observable.just(currentFacility))
    whenever(protocolRepository.dosagesForDrug(drugName, protocolUuid)).thenReturn(Observable.just(listOf("5 mg", "10 mg")))

    uiEvents.onNext(PrescribedDrugsWithDosagesSheetCreated(drugName, patientUUID))

    verify(screen).populateDosageList(listOf(
        PrescribedDosageListItem(DosageType.Dosage("5 mg")),
        PrescribedDosageListItem(DosageType.Dosage("10 mg")),
        PrescribedDosageListItem(DosageType.None)
    ))
  }

  @Test
  fun `when a dosage is selected, it should be saved as prescription`() {
    val protocolUuid = UUID.randomUUID()
    val currentFacility = PatientMocker.facility(protocolUuid = protocolUuid)
    val patientUUID = UUID.randomUUID()
    val drugName = "Amlodipine"
    val dosageSelected = "5mg"
    val rxNormCode = "rxNormCode-1"

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(PatientMocker.loggedInUser()))
    whenever(facilityRepository.currentFacility(any<User>())).thenReturn(Observable.just(currentFacility))
    whenever(protocolRepository.drugByNameAndDosage(drugName, dosageSelected, protocolUuid)).thenReturn(
        PatientMocker.protocolDrug(name = drugName, dosage = dosageSelected, rxNormCode = rxNormCode))
    whenever(protocolRepository.dosagesForDrug(drugName, protocolUuid)).thenReturn(Observable.just(listOf("5 mg", "10 mg")))
    whenever(prescriptionRepository.savePrescription(any(), any())).thenReturn(Completable.complete())

    uiEvents.onNext(PrescribedDrugsWithDosagesSheetCreated(drugName, patientUUID))
    uiEvents.onNext(DosageSelected(dosageSelected))

    verify(prescriptionRepository, times(1)).savePrescription(patientUUID, drugName, dosageSelected, rxNormCode, true)
  }
}
