package org.simple.clinic.drugs.selection

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.selection.dosage.DosageListItem
import org.simple.clinic.drugs.selection.dosage.DosageOption
import org.simple.clinic.drugs.selection.dosage.DosagePickerSheet
import org.simple.clinic.drugs.selection.dosage.DosagePickerSheetController
import org.simple.clinic.drugs.selection.dosage.DosagePickerSheetCreated
import org.simple.clinic.drugs.selection.dosage.DosageSelected
import org.simple.clinic.drugs.selection.dosage.NoneSelected
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class DosagePickerSheetControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val protocolRepository = mock<ProtocolRepository>()
  private val userSession = mock<UserSession>()
  private val sheet = mock<DosagePickerSheet>()
  private val facilityRepository = mock<FacilityRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val protocolUuid = UUID.randomUUID()
  private val user = PatientMocker.loggedInUser()
  private val currentFacility = PatientMocker.facility(protocolUuid = protocolUuid)
  private val userSubject = PublishSubject.create<User>()

  private val controller = DosagePickerSheetController(userSession, facilityRepository, protocolRepository, prescriptionRepository)

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser()).thenReturn(userSubject)
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(currentFacility))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(sheet) }

    userSubject.onNext(user)
  }

  @Test
  fun `when sheet is created, list of dosages for that drug should be displayed`() {
    val patientUUID = UUID.randomUUID()
    val drugName = "Amlodipine"

    val protocolDrug1 = PatientMocker.protocolDrug(name = drugName, dosage = "5 mg")
    val protocolDrug2 = PatientMocker.protocolDrug(name = drugName, dosage = "10 mg")

    whenever(protocolRepository.drugsByNameOrDefault(drugName, protocolUuid)).thenReturn(Observable.just(listOf(protocolDrug1, protocolDrug2)))

    uiEvents.onNext(DosagePickerSheetCreated(drugName, patientUUID, None))
    userSubject.onNext(user)

    verify(sheet).populateDosageList(listOf(
        DosageListItem(DosageOption.Dosage(protocolDrug1)),
        DosageListItem(DosageOption.Dosage(protocolDrug2)),
        DosageListItem(DosageOption.None)
    ))
  }

  @Test
  fun `when a dosage is selected, it should be saved as prescription`() {
    val patientUUID = UUID.randomUUID()
    val drugName = "Amlodipine"
    val dosageSelected = PatientMocker.protocolDrug(name = drugName, dosage = "5 mg")

    whenever(protocolRepository.drugsByNameOrDefault(drugName, protocolUuid)).thenReturn(Observable.never())
    whenever(prescriptionRepository.savePrescription(patientUUID, dosageSelected, currentFacility)).thenReturn(Completable.complete())

    uiEvents.onNext(DosagePickerSheetCreated(drugName, patientUUID, None))
    uiEvents.onNext(DosageSelected(dosageSelected))

    verify(prescriptionRepository, times(1)).savePrescription(patientUUID, dosageSelected, currentFacility)
    verify(prescriptionRepository, never()).softDeletePrescription(any())
    verify(sheet).finish()
  }

  @Test
  fun `when a dosage is selected and a prescription exists for that drug, existing prescription should get deleted and new prescription should be saved`() {
    val patientUUID = UUID.randomUUID()
    val drugName = "Amlodipine"
    val dosageSelected = PatientMocker.protocolDrug(name = drugName, dosage = "5 mg")
    val existingPrescription = PatientMocker.prescription(name = drugName, dosage = "10 mg")

    whenever(protocolRepository.drugsByNameOrDefault(drugName, protocolUuid)).thenReturn(Observable.never())
    whenever(prescriptionRepository.savePrescription(patientUUID, dosageSelected, currentFacility)).thenReturn(Completable.complete())
    whenever(prescriptionRepository.softDeletePrescription(existingPrescription.uuid)).thenReturn(Completable.complete())

    uiEvents.onNext(DosagePickerSheetCreated(drugName, patientUUID, existingPrescription.uuid.toOptional()))
    uiEvents.onNext(DosageSelected(dosageSelected))

    verify(prescriptionRepository, times(1)).softDeletePrescription(existingPrescription.uuid)
    verify(prescriptionRepository, times(1)).savePrescription(patientUUID, dosageSelected, currentFacility)
    verify(sheet).finish()
  }

  @Test
  fun `when none is selected, the existing prescription should be soft deleted`() {
    val patientUUID = UUID.randomUUID()
    val drugName = "Amlodipine"
    val existingPrescription = PatientMocker.prescription(name = drugName, dosage = "10 mg")

    whenever(protocolRepository.drugsByNameOrDefault(drugName, protocolUuid)).thenReturn(Observable.never())
    whenever(prescriptionRepository.softDeletePrescription(existingPrescription.uuid)).thenReturn(Completable.complete())

    uiEvents.onNext(DosagePickerSheetCreated(drugName, patientUUID, existingPrescription.uuid.toOptional()))
    uiEvents.onNext(NoneSelected)

    verify(prescriptionRepository, times(1)).softDeletePrescription(existingPrescription.uuid)
    verify(prescriptionRepository, never()).savePrescription(any(), any(), any())
  }
}
