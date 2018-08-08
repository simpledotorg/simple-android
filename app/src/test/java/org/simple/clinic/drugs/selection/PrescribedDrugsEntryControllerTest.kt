package org.simple.clinic.drugs.selection

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.protocol.Protocol
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class PrescribedDrugsEntryControllerTest {

  private val screen = mock<PrescribedDrugsScreen>()
  private val protocolRepository = mock<ProtocolRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientUuid = UUID.randomUUID()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: PrescribedDrugsEntryController

  @Before
  fun setUp() {
    controller = PrescribedDrugsEntryController(protocolRepository, prescriptionRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `should correctly construct RecyclerView models from protocol drugs and prescribed drugs`() {
    val amlodipine = PatientMocker.protocolDrug(name = "Amlodipine", dosages = listOf("5mg", "10mg"))
    val telmisartan = PatientMocker.protocolDrug(name = "Telmisartan", dosages = listOf("40mg", "80mg"))
    val chlorthalidone = PatientMocker.protocolDrug(name = "Chlorthalidone", dosages = listOf("12.5mg", "25mg"))

    val protocol = Protocol(
        mock(),
        name = "Dummy protocol",
        drugs = listOf(amlodipine, telmisartan, chlorthalidone))
    whenever(protocolRepository.currentProtocol()).thenReturn(Observable.just(protocol))

    val prescriptionUuid1 = UUID.randomUUID()
    val prescriptionUuid2 = UUID.randomUUID()
    val prescriptionUuid3 = UUID.randomUUID()
    val prescriptionUuid4 = UUID.randomUUID()

    val amlodipinePrescription = PatientMocker.prescription(name = "Amlodipine", dosage = "10mg", isProtocolDrug = true)
    val telmisartanPrescription = PatientMocker.prescription(uuid = prescriptionUuid1, name = "Telmisartan", dosage = "9000mg", isProtocolDrug = false)
    val ReesesPrescription = PatientMocker.prescription(uuid = prescriptionUuid2, name = "Reese's", dosage = "5 packets", isProtocolDrug = false)
    val fooPrescription = PatientMocker.prescription(uuid = prescriptionUuid3, name = "Foo", dosage = "2 pills", isProtocolDrug = false)
    val barPrescription = PatientMocker.prescription(uuid = prescriptionUuid4, name = "Bar", dosage = null, isProtocolDrug = false)

    val prescriptions = listOf(
        amlodipinePrescription,
        telmisartanPrescription,
        ReesesPrescription,
        fooPrescription,
        barPrescription)
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(prescriptions))

    uiEvents.onNext(PrescribedDrugsScreenCreated(patientUuid))

    val expectedProtocolDrugUiModels = listOf(
        ProtocolDrugSelectionListItem(
            0,
            drug = amlodipine,
            option1 = ProtocolDrugSelectionListItem.DosageOption.Unselected(amlodipine.dosages[0]),
            option2 = ProtocolDrugSelectionListItem.DosageOption.Selected(amlodipine.dosages[1], prescription = amlodipinePrescription)),
        ProtocolDrugSelectionListItem(
            1,
            drug = telmisartan,
            option1 = ProtocolDrugSelectionListItem.DosageOption.Unselected(telmisartan.dosages[0]),
            option2 = ProtocolDrugSelectionListItem.DosageOption.Unselected(telmisartan.dosages[1])),
        ProtocolDrugSelectionListItem(
            2,
            drug = chlorthalidone,
            option1 = ProtocolDrugSelectionListItem.DosageOption.Unselected(chlorthalidone.dosages[0]),
            option2 = ProtocolDrugSelectionListItem.DosageOption.Unselected(chlorthalidone.dosages[1])),
        CustomPrescribedDrugListItem(telmisartanPrescription),
        CustomPrescribedDrugListItem(ReesesPrescription),
        CustomPrescribedDrugListItem(fooPrescription),
        CustomPrescribedDrugListItem(barPrescription))
    verify(screen).populateDrugsList(expectedProtocolDrugUiModels)
  }

  @Test
  fun `when a protocol drug is selected then a prescription should be saved for it`() {
    whenever(prescriptionRepository.savePrescription(any(), any(), any())).thenReturn(Completable.complete())
    whenever(protocolRepository.currentProtocol()).thenReturn(Observable.never())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())

    val amlodipine = PatientMocker.protocolDrug(name = "Amlodipine", dosages = listOf("5mg", "10mg"))

    uiEvents.onNext(PrescribedDrugsScreenCreated(patientUuid))
    uiEvents.onNext(ProtocolDrugDosageSelected(amlodipine, "10mg"))

    verify(prescriptionRepository).savePrescription(patientUuid, drug = amlodipine, dosage = "10mg")
  }

  @Test
  fun `when a protocol drug is unselected then its prescription should be soft deleted`() {
    val amlodipine = PatientMocker.protocolDrug(name = "Amlodipine", dosages = listOf("5mg", "10mg"))
    val unselectedPrescriptionId = UUID.randomUUID()

    whenever(prescriptionRepository.savePrescription(any(), any(), any())).thenReturn(Completable.complete())
    whenever(prescriptionRepository.softDeletePrescription(unselectedPrescriptionId)).thenReturn(Completable.complete())

    whenever(protocolRepository.currentProtocol()).thenReturn(Observable.never())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())

    uiEvents.onNext(PrescribedDrugsScreenCreated(patientUuid))
    uiEvents.onNext(ProtocolDrugDosageSelected(amlodipine, "10mg"))
    uiEvents.onNext(ProtocolDrugDosageSelected(amlodipine, "5mg"))
    uiEvents.onNext(ProtocolDrugDosageUnselected(
        drug = amlodipine,
        prescription = PatientMocker.prescription(unselectedPrescriptionId, amlodipine.name, "10mg")))

    verify(prescriptionRepository).savePrescription(patientUuid, drug = amlodipine, dosage = "10mg")
    verify(prescriptionRepository).savePrescription(patientUuid, drug = amlodipine, dosage = "5mg")
    verify(prescriptionRepository).softDeletePrescription(unselectedPrescriptionId)
  }

  @Test
  fun `when new prescription button is clicked then prescription entry sheet should be shown`() {
    whenever(protocolRepository.currentProtocol()).thenReturn(Observable.never())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())

    uiEvents.onNext(PrescribedDrugsScreenCreated(patientUuid))
    uiEvents.onNext(AddNewPrescriptionClicked())

    verify(screen).showNewPrescriptionEntrySheet(patientUuid)
  }

  @Test
  fun `when a custom prescription delete is clicked then delete confirmation dialog should be shown`() {
    val prescription = PatientMocker.prescription(name = "Amlodipine", dosage = "10mg")
    uiEvents.onNext(DeleteCustomPrescriptionClicked(prescription))

    verify(screen).showDeleteConfirmationDialog(prescription)
  }
}
