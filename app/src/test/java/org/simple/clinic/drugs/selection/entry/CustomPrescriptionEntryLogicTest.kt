package org.simple.clinic.drugs.selection.entry

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntryUiRenderer.Companion.DOSAGE_PLACEHOLDER
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class CustomPrescriptionEntryLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()
  private val ui = mock<CustomPrescriptionEntryUi>()
  private val uiActions = mock<CustomPrescriptionEntryUiActions>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private val facility = TestData.facility(uuid = UUID.fromString("951269a6-a960-468c-96ef-a95a6706c91e"))
  private val patientUuid = UUID.fromString("a90376d0-e29a-428f-80dc-bd4bdd74d9bf")
  private val prescriptionUuid = UUID.fromString("eef2b1c9-52cd-43d9-b109-b120b0e4c16c")
  private val updatePrescription = TestData.prescription(uuid = prescriptionUuid)
  private val uuidGenerator = FakeUuidGenerator.fixed(prescriptionUuid)

  private lateinit var fixture: MobiusTestFixture<CustomPrescriptionEntryModel, CustomPrescriptionEntryEvent, CustomPrescriptionEntryEffect>

  @After
  fun tearDown() {
    fixture.dispose()
  }

  @Test
  fun `save should remain disabled while drug name is empty`() {
    //when
    createSheetForNewPrescription(patientUuid)
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged(""))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged(""))

    //then
    verify(ui, times(1)).setSaveButtonEnabled(false)
  }

  @Test
  fun `save should be enabled when drug name is not empty`() {
    //when
    createSheetForNewPrescription(patientUuid)
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("A"))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Am"))

    //then
    verify(ui, times(1)).setSaveButtonEnabled(true)
  }

  @Test
  @Parameters(value = ["", "10mg"])
  fun `when sheet is opened in new mode and save is clicked then a new prescription should be saved`(
      dosage: String
  ) {
    //given
    whenever(prescriptionRepository.savePrescription(
        uuid = prescriptionUuid,
        patientUuid = patientUuid,
        name = "Amlodipine",
        dosage = dosage.nullIfBlank(),
        rxNormCode = null,
        isProtocolDrug = false,
        facility = facility
    )).thenReturn(Completable.complete())

    //when
    createSheetForNewPrescription(patientUuid)
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Amlodipine"))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(dosage))
    uiEvents.onNext(SaveCustomPrescriptionClicked)

    //then
    verify(prescriptionRepository).savePrescription(
        uuid = prescriptionUuid,
        patientUuid = patientUuid,
        name = "Amlodipine",
        dosage = dosage.nullIfBlank(),
        rxNormCode = null,
        isProtocolDrug = false,
        facility = facility
    )
    verify(uiActions).finish()
  }

  @Test
  fun `when dosage field is focused and empty then placeholder value for dosage should be shown and cursor should be moved to the start`() {
    //when
    createSheetForNewPrescription(patientUuid)
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(""))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))

    //then
    verify(ui, times(1)).setDrugDosageText(eq(DOSAGE_PLACEHOLDER))
    verify(ui).moveDrugDosageCursorToBeginning()
    verify(ui, never()).setDrugDosageText(eq(""))
  }

  @Test
  fun `value for dosage should be reset when dosage field is not focused and empty`() {
    //when
    createSheetForNewPrescription(patientUuid)
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("$DOSAGE_PLACEHOLDER"))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))

    //then
    verify(ui, times(1)).setDrugDosageText(eq(""))
    verify(ui, never()).setDrugDosageText(eq(DOSAGE_PLACEHOLDER))
  }

  @Test
  fun `when sheet is opened for a new entry then enter new medicine title should be shown`() {
    //when
    createSheetForNewPrescription(patientUuid)

    //then
    verify(ui).showEnterNewPrescriptionTitle()
  }

  @Test
  fun `when sheet is opened to update a medicine then update medicine title should be shown`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(updatePrescription))
    whenever(prescriptionRepository.prescriptionImmediate(prescriptionUuid)).thenReturn(updatePrescription)

    //when
    createSheetForUpdatingPrescription(prescriptionUuid)

    //then
    verify(ui).showEditPrescriptionTitle()
  }

  @Test
  fun `the remove button should show when the sheet is opened for edit`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(updatePrescription))
    whenever(prescriptionRepository.prescriptionImmediate(prescriptionUuid)).thenReturn(updatePrescription)

    //when
    createSheetForUpdatingPrescription(prescriptionUuid)

    //then
    verify(ui).showRemoveButton()
  }

  @Test
  fun `the remove button should be hidden when the sheet is opened for new entry`() {
    //when
    createSheetForNewPrescription(patientUuid)

    //then
    verify(ui).hideRemoveButton()
  }

  @Test
  fun `when sheet is opened to edit prescription then the drug name and dosage should be pre-filled`() {
    //given
    val prescription = TestData.prescription(uuid = prescriptionUuid)
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(prescription))
    whenever(prescriptionRepository.prescriptionImmediate(prescriptionUuid)).thenReturn(prescription)

    //when
    createSheetForUpdatingPrescription(prescriptionUuid)

    //then
    verify(uiActions).setMedicineName(prescription.name)
    verify(uiActions).setDosage(prescription.dosage)
  }

  @Test
  fun `when remove is clicked, then show confirmation dialog`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(updatePrescription))
    whenever(prescriptionRepository.prescriptionImmediate(prescriptionUuid)).thenReturn(updatePrescription)

    //when
    createSheetForUpdatingPrescription(prescriptionUuid)
    uiEvents.onNext(RemoveCustomPrescriptionClicked)

    //then
    verify(uiActions).showConfirmRemoveMedicineDialog(prescriptionUuid)
  }

  @Test
  fun `when prescription is deleted then close the sheet`() {
    //given
    val prescription = TestData.prescription(uuid = prescriptionUuid, isDeleted = true)
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(prescription))
    whenever(prescriptionRepository.prescriptionImmediate(prescriptionUuid)).thenReturn(prescription)

    //when
    createSheetForUpdatingPrescription(prescriptionUuid)

    //then
    verify(uiActions).finish()
  }

  private fun createSheetForNewPrescription(patientUuid: UUID) {
    val openAsNew = OpenAs.New(patientUuid)
    instantiateFixture(openAsNew)
  }

  private fun createSheetForUpdatingPrescription(
      prescriptionUuid: UUID,
      uuidGenerator: UuidGenerator = this.uuidGenerator
  ) {
    val openAsUpdate = OpenAs.Update(patientUuid, prescriptionUuid)
    instantiateFixture(openAsUpdate, uuidGenerator)
  }

  private fun instantiateFixture(
      openAs: OpenAs,
      uuidGenerator: UuidGenerator = this.uuidGenerator
  ) {
    val uiRenderer = CustomPrescriptionEntryUiRenderer(ui)
    val effectHandler = CustomPrescriptionEntryEffectHandler(
        uiActions = uiActions,
        schedulersProvider = TrampolineSchedulersProvider(),
        prescriptionRepository = prescriptionRepository,
        currentFacility = Lazy { facility },
        uuidGenerator = uuidGenerator
    )

    fixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = CustomPrescriptionEntryModel.create(openAs),
        init = CustomPrescriptionEntryInit(),
        update = CustomPrescriptionEntryUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
    fixture.start()
  }
}
