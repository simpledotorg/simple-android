package org.simple.clinic.drugs.entry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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

  private val screen = mock<PrescribedDrugsEntryScreen>()
  private val protocolRepository = mock<ProtocolRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()

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

    val patientUuid = UUID.randomUUID()

    val prescriptionUuid1 = UUID.randomUUID()
    val prescriptionUuid2 = UUID.randomUUID()
    val prescriptionUuid3 = UUID.randomUUID()
    val prescriptionUuid4 = UUID.randomUUID()

    val prescriptions = listOf(
        PatientMocker.prescription(name = "Amlodipine", dosage = "10mg"),
        PatientMocker.prescription(uuid = prescriptionUuid1, name = "Telmisartan", dosage = "9000mg"),
        PatientMocker.prescription(uuid = prescriptionUuid2, name = "Reese's", dosage = "5 packets"),
        PatientMocker.prescription(uuid = prescriptionUuid3, name = "Foo", dosage = "2 pills"),
        PatientMocker.prescription(uuid = prescriptionUuid4, name = "Bar", dosage = null))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(prescriptions))

    uiEvents.onNext(PrescribedDrugsEntryScreenCreated(patientUuid))

    val expectedProtocolDrugUiModels = listOf(
        ProtocolDrugSelectionItem(
            0,
            name = amlodipine.name,
            option1 = ProtocolDrugSelectionItem.DosageOption(amlodipine.dosages[0], isSelected = false),
            option2 = ProtocolDrugSelectionItem.DosageOption(amlodipine.dosages[1], isSelected = true)),
        ProtocolDrugSelectionItem(
            1,
            name = telmisartan.name,
            option1 = ProtocolDrugSelectionItem.DosageOption(telmisartan.dosages[0], isSelected = false),
            option2 = ProtocolDrugSelectionItem.DosageOption(telmisartan.dosages[1], isSelected = false)),
        ProtocolDrugSelectionItem(
            2,
            name = chlorthalidone.name,
            option1 = ProtocolDrugSelectionItem.DosageOption(chlorthalidone.dosages[0], isSelected = false),
            option2 = ProtocolDrugSelectionItem.DosageOption(chlorthalidone.dosages[1], isSelected = false)),
        CustomPrescribedDrugItem(
            prescriptionUuid1,
            name = "Telmisartan",
            dosage = "9000mg"),
        CustomPrescribedDrugItem(
            prescriptionUuid2,
            name = "Reese's",
            dosage = "5 packets"),
        CustomPrescribedDrugItem(
            prescriptionUuid3,
            name = "Foo",
            dosage = "2 pills"),
        CustomPrescribedDrugItem(
            prescriptionUuid4,
            name = "Bar",
            dosage = null))
    verify(screen).populateDrugsList(expectedProtocolDrugUiModels)
  }

  @Test
  fun `when a protocol drug is selected then a prescription should be saved for it`() {
    // TODO.
  }

  @Test
  fun `when new prescription button is clicked then prescription entry sheet should be shown`() {
    // TODO.
  }

  @Test
  fun `when delete prescription is clicked then the prescription should be marked as soft-deleted`() {
    // TODO.
  }
}
