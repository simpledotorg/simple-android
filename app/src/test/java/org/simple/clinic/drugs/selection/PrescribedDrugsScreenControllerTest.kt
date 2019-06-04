package org.simple.clinic.drugs.selection

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
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
import org.simple.clinic.drugs.selection.entry.CustomPrescribedDrugListItem
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PrescribedDrugsScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<PrescribedDrugScreen>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val protocolRepository = mock<ProtocolRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientUuid = UUID.randomUUID()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: PrescribedDrugsScreenController

  @Before
  fun setUp() {
    controller = PrescribedDrugsScreenController(userSession, facilityRepository, protocolRepository, prescriptionRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `should correctly construct RecyclerView models from protocol drugs and prescribed drugs`() {
    val amlodipine5mg = PatientMocker.protocolDrug(name = "Amlodipine", dosage = "5mg")
    val amlodipine10mg = PatientMocker.protocolDrug(name = "Amlodipine", dosage = "10mg")
    val telmisartan40mg = PatientMocker.protocolDrug(name = "Telmisartan", dosage = "40mg")
    val telmisartan80mg = PatientMocker.protocolDrug(name = "Telmisartan", dosage = "80mg")

    val protocolUuid = UUID.randomUUID()
    val currentFacility = PatientMocker.facility(protocolUuid = protocolUuid)
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(PatientMocker.loggedInUser()))
    whenever(facilityRepository.currentFacility(any<User>())).thenReturn(Observable.just(currentFacility))

    whenever(protocolRepository.drugsForProtocolOrDefault(protocolUuid)).thenReturn(Observable.just(listOf(
        ProtocolDrugAndDosages(amlodipine10mg.name, listOf(amlodipine5mg, amlodipine10mg)),
        ProtocolDrugAndDosages(telmisartan40mg.name, listOf(telmisartan40mg, telmisartan80mg))
    )))

    val prescriptionUuid1 = UUID.randomUUID()
    val prescriptionUuid2 = UUID.randomUUID()
    val prescriptionUuid3 = UUID.randomUUID()
    val prescriptionUuid4 = UUID.randomUUID()

    val amlodipine10mgPrescription = PatientMocker.prescription(name = "Amlodipine", dosage = "10mg", isProtocolDrug = true)
    val telmisartan9000mgPrescription = PatientMocker.prescription(uuid = prescriptionUuid1, name = "Telmisartan", dosage = "9000mg", isProtocolDrug = false)
    val reesesPrescription = PatientMocker.prescription(uuid = prescriptionUuid2, name = "Reese's", dosage = "5 packets", isProtocolDrug = false)
    val fooPrescription = PatientMocker.prescription(uuid = prescriptionUuid3, name = "Foo", dosage = "2 pills", isProtocolDrug = false)
    val barPrescription = PatientMocker.prescription(uuid = prescriptionUuid4, name = "Bar", dosage = null, isProtocolDrug = false)

    val prescriptions = listOf(
        amlodipine10mgPrescription,
        telmisartan9000mgPrescription,
        reesesPrescription,
        fooPrescription,
        barPrescription)
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(prescriptions))

    uiEvents.onNext(PrescribedDrugsScreenCreated(patientUuid))

    val expectedUiModels = listOf(
        ProtocolDrugListItem(
            id = 0,
            drugName = amlodipine10mg.name,
            prescribedDrug = amlodipine10mgPrescription,
            hideDivider = false),
        ProtocolDrugListItem(
            id = 1,
            drugName = telmisartan40mg.name,
            prescribedDrug = null,
            hideDivider = false),
        CustomPrescribedDrugListItem(telmisartan9000mgPrescription, false),
        CustomPrescribedDrugListItem(reesesPrescription, false),
        CustomPrescribedDrugListItem(fooPrescription, false),
        CustomPrescribedDrugListItem(barPrescription, true))
    verify(screen).populateDrugsList(expectedUiModels)
  }

  @Test
  fun `when new prescription button is clicked then prescription entry sheet should be shown`() {
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.never())
    whenever(protocolRepository.drugsForProtocolOrDefault(any())).thenReturn(Observable.never())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())

    uiEvents.onNext(PrescribedDrugsScreenCreated(patientUuid))
    uiEvents.onNext(AddNewPrescriptionClicked)

    verify(screen).showNewPrescriptionEntrySheet(patientUuid)
  }

  @Parameters(
      "Amlodipine",
      "Telimisartan",
      "Athenlol"
  )
  @Test
  fun `when a protocol drug is selected then open dosages sheet for that drug`(drugName: String) {
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.never())
    whenever(prescriptionRepository.savePrescription(any(), any(), any())).thenReturn(Completable.complete())
    whenever(protocolRepository.drugsForProtocolOrDefault(any())).thenReturn(Observable.never())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())

    uiEvents.onNext(PrescribedDrugsScreenCreated(patientUuid))
    uiEvents.onNext(ProtocolDrugClicked(drugName = drugName, prescriptionForProtocolDrug = null))

    verify(screen).showDosageSelectionSheet(drugName = drugName, patientUuid = patientUuid, prescribedDrugUuid = null)
  }

  @Test
  fun `when a custom prescription is clicked then open upate custom prescription screen`() {
    val prescribedDrug = PatientMocker.prescription()
    uiEvents.onNext(CustomPrescriptionClicked(prescribedDrug))

    verify(screen).showUpdateCustomPrescriptionSheet(prescribedDrug)
  }
}
