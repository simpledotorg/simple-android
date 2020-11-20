package org.simple.clinic.drugs.selection

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.drugs.AddNewPrescriptionClicked
import org.simple.clinic.drugs.CustomPrescriptionClicked
import org.simple.clinic.drugs.EditMedicinesEffect
import org.simple.clinic.drugs.EditMedicinesEffectHandler
import org.simple.clinic.drugs.EditMedicinesEvent
import org.simple.clinic.drugs.EditMedicinesInit
import org.simple.clinic.drugs.EditMedicinesModel
import org.simple.clinic.drugs.EditMedicinesUiRenderer
import org.simple.clinic.drugs.EditMedicinesUpdate
import org.simple.clinic.drugs.OpenIntention.RefillMedicine
import org.simple.clinic.drugs.PrescribedDrugsDoneClicked
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.ProtocolDrugClicked
import org.simple.clinic.drugs.selection.entry.CustomPrescribedDrugListItem
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class EditMedicinesScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<EditMedicinesUi>()
  private val uiActions = mock<EditMedicinesUiActions>()
  private val protocolRepository = mock<ProtocolRepository>()
  private val uuidGenerator = mock<UuidGenerator>()
  private val utcClock = mock<UtcClock>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientUuid = UUID.fromString("2e9a1721-5472-4ebb-9d1a-7e707645eb7b")
  private val protocolUuid = UUID.fromString("905a545c-1988-441b-9139-11ae00579883")
  val loggedInUser = TestData.loggedInUser(uuid = UUID.fromString("eb1741f5-ed0e-436b-9e73-43713a4989c6"))
  val facility = TestData.facility(
      uuid = UUID.fromString("a10425cb-88b6-4de9-9457-c426f5e6cfbb"),
      protocolUuid = protocolUuid
  )
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val appointmentRepository = mock<AppointmentRepository>()

  private lateinit var fixture: MobiusTestFixture<EditMedicinesModel, EditMedicinesEvent, EditMedicinesEffect>

  @Before
  fun setup() {
    val editMedicinesUiRenderer = EditMedicinesUiRenderer(ui)
    val effectHandler = EditMedicinesEffectHandler(
        uiActions = uiActions,
        schedulersProvider = TrampolineSchedulersProvider(),
        protocolRepository = protocolRepository,
        prescriptionRepository = prescriptionRepository,
        facility = Lazy { facility },
        utcClock = utcClock,
        uuidGenerator = uuidGenerator,
        appointmentsRepository = appointmentRepository
    )

    fixture = MobiusTestFixture(
        uiEvents.ofType(),
        EditMedicinesModel.create(patientUuid, RefillMedicine),
        EditMedicinesInit(),
        EditMedicinesUpdate(LocalDate.of(2020,11,12), ZoneOffset.UTC),
        effectHandler.build(),
        editMedicinesUiRenderer::render
    )
  }

  @After
  fun tearDown() {
    fixture.dispose()
  }

  @Test
  fun `should correctly construct RecyclerView models from protocol drugs and prescribed drugs`() {
    //given
    val amlodipine5mg = TestData.protocolDrug(name = "Amlodipine", dosage = "5mg")
    val amlodipine10mg = TestData.protocolDrug(name = "Amlodipine", dosage = "10mg")
    val telmisartan40mg = TestData.protocolDrug(name = "Telmisartan", dosage = "40mg")
    val telmisartan80mg = TestData.protocolDrug(name = "Telmisartan", dosage = "80mg")

    whenever(protocolRepository.drugsForProtocolOrDefault(protocolUuid)).thenReturn(listOf(
        ProtocolDrugAndDosages(amlodipine10mg.name, listOf(amlodipine5mg, amlodipine10mg)),
        ProtocolDrugAndDosages(telmisartan40mg.name, listOf(telmisartan40mg, telmisartan80mg))
    ))

    val amlodipine10mgPrescription = TestData.prescription(
        uuid = UUID.fromString("90e28866-90f6-48a0-add1-cf44aa43209c"),
        name = "Amlodipine",
        dosage = "10mg",
        isProtocolDrug = true
    )
    val telmisartan9000mgPrescription = TestData.prescription(
        uuid = UUID.fromString("ac3cfff0-2ebf-4c9c-adab-a41cc8a0bbeb"),
        name = "Telmisartan",
        dosage = "9000mg",
        isProtocolDrug = false
    )
    val reesesPrescription = TestData.prescription(
        uuid = UUID.fromString("34e466e2-3995-47b4-b1af-f4d7ea58d18c"),
        name = "Reese's",
        dosage = "5 packets",
        isProtocolDrug = false
    )
    val fooPrescription = TestData.prescription(
        uuid = UUID.fromString("68dc8060-bed4-4e1b-9891-7d77cad9639e"),
        name = "Foo",
        dosage = "2 pills",
        isProtocolDrug = false
    )
    val barPrescription = TestData.prescription(
        uuid = UUID.fromString("b5eb5dfa-f131-4d9f-a2d2-41d56aa109da"),
        name = "Bar",
        dosage = null,
        isProtocolDrug = false
    )

    val prescriptions = listOf(
        amlodipine10mgPrescription,
        telmisartan9000mgPrescription,
        reesesPrescription,
        fooPrescription,
        barPrescription)
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(prescriptions))

    //when
    setupController()

    //then
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

    verify(ui).populateDrugsList(expectedUiModels)
  }

  @Test
  fun `when new prescription button is clicked then prescription entry sheet should be shown`() {
    //given
    whenever(protocolRepository.drugsForProtocolOrDefault(protocolUuid)).thenReturn(emptyList())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())

    //when
    setupController()
    uiEvents.onNext(AddNewPrescriptionClicked)

    //then
    verify(uiActions).showNewPrescriptionEntrySheet(patientUuid)
  }

  @Parameters(
      "Amlodipine",
      "Telimisartan",
      "Athenlol"
  )
  @Test
  fun `when a protocol drug is selected then open dosages sheet for that drug`(drugName: String) {
    //given
    val protocolDrug = TestData.protocolDrug(uuid = UUID.fromString("362c6a00-3ed9-4b7a-b22a-9168b736bd35"), name = drugName)

    whenever(protocolRepository.drugsForProtocolOrDefault(protocolUuid)).thenReturn(emptyList())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())

    //when
    setupController()
    uiEvents.onNext(ProtocolDrugClicked(drugName = drugName, prescription = null))

    //then
    verify(uiActions).showDosageSelectionSheet(drugName = drugName, patientUuid = patientUuid, prescribedDrugUuid = null)
  }

  @Test
  fun `when a custom prescription is clicked then open update custom prescription screen`() {
    //given
    val prescribedDrug = TestData.prescription()
    whenever(protocolRepository.drugsForProtocolOrDefault(protocolUuid)).thenReturn(emptyList())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())


    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionClicked(prescribedDrug))

    //then
    verify(uiActions).showUpdateCustomPrescriptionSheet(prescribedDrug)
  }

  @Test
  fun `when done click event is received then go back to patient summary`() {
    //given
    whenever(protocolRepository.drugsForProtocolOrDefault(protocolUuid)).thenReturn(emptyList())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.empty())

    //when
    setupController()
    uiEvents.onNext(PrescribedDrugsDoneClicked)

    //then
    verify(uiActions).goBackToPatientSummary()
  }

  private fun setupController() {
    fixture.start()

    uiEvents.onNext(ScreenCreated())
  }
}
