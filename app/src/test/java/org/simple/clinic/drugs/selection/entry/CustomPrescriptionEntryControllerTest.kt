package org.simple.clinic.drugs.selection.entry

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
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
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class CustomPrescriptionEntryControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()
  private val ui = mock<CustomPrescriptionEntryUi>()
  private val uiActions = mock<CustomPrescriptionEntryUiActions>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientUuid = UUID.fromString("a90376d0-e29a-428f-80dc-bd4bdd74d9bf")
  private val prescriptionUuid = UUID.fromString("eef2b1c9-52cd-43d9-b109-b120b0e4c16c")
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val user = TestData.loggedInUser()
  private val facility = TestData.facility()

  lateinit var controller: CustomPrescriptionEntryController
  private lateinit var fixture: MobiusTestFixture<CustomPrescriptionEntryModel, CustomPrescriptionEntryEvent, CustomPrescriptionEntryEffect>

  @Before
  fun setUp() {
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)
    whenever(facilityRepository.currentFacilityImmediate(user)).thenReturn(facility)
  }

  @After
  fun tearDown() {
    fixture.dispose()
  }

  @Test
  fun `save should remain disabled while drug name is empty`() {
    //when
    setupController(OpenAs.New(patientUuid))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged(""))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged(""))

    //then
    verify(ui, times(1)).setSaveButtonEnabled(false)
  }

  @Test
  fun `save should be enabled when drug name is not empty`() {
    //when
    setupController(OpenAs.New(patientUuid))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("A"))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Am"))

    //then
    verify(ui, times(1)).setSaveButtonEnabled(true)
  }

  @Test
  @Parameters(value = ["", "10mg"])
  fun `when sheet is opened in new mode and save is clicked then a new prescription should be saved`(dosage: String) {
    //given
    whenever(prescriptionRepository.savePrescription(
        patientUuid = patientUuid,
        name = "Amlodipine",
        dosage = dosage.nullIfBlank(),
        rxNormCode = null,
        isProtocolDrug = false,
        facility = facility
    )).thenReturn(Completable.complete())

    //when
    setupController(OpenAs.New(patientUuid))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Amlodipine"))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(dosage))
    uiEvents.onNext(SaveCustomPrescriptionClicked)

    //then
    verify(prescriptionRepository).savePrescription(
        patientUuid = patientUuid,
        name = "Amlodipine",
        dosage = dosage.nullIfBlank(),
        rxNormCode = null,
        isProtocolDrug = false,
        facility = facility
    )
    verify(prescriptionRepository, never()).updatePrescription(any())
    verify(uiActions).finish()
  }

  @Test
  fun `placeholder value for dosage should be shown when dosage field is focused and empty`() {
    //when
    setupController(OpenAs.New(patientUuid))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(""))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))

    //then
    verify(ui, times(1)).setDrugDosageText(eq(DOSAGE_PLACEHOLDER))
    verify(ui, never()).setDrugDosageText(eq(""))
  }

  @Test
  fun `value for dosage should be reset when dosage field is not focused and empty`() {
    //when
    setupController(OpenAs.New(patientUuid))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("$DOSAGE_PLACEHOLDER"))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))

    //then
    verify(ui, times(1)).setDrugDosageText(eq(""))
    verify(ui, never()).setDrugDosageText(eq(DOSAGE_PLACEHOLDER))
  }

  @Test
  fun `when dosage field is focused and the placeholder value is set then the cursor should be moved to the beginning`() {
    //when
    setupController(OpenAs.New(patientUuid))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("$DOSAGE_PLACEHOLDER"))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))

    //then
    verify(ui).moveDrugDosageCursorToBeginning()
  }

  @Test
  fun `when sheet is opened for a new entry then enter new medicine title should be shown`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController(OpenAs.New(patientUuid))

    //then
    verify(ui).showEnterNewPrescriptionTitle()
  }

  @Test
  fun `when sheet is opened to update a medicine then update medicine title should be shown`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController(OpenAs.Update(prescriptionUuid))

    //then
    verify(ui).showEditPrescriptionTitle()
  }

  @Test
  fun `the remove button should show when the sheet is opened for edit`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController(OpenAs.Update(prescriptionUuid))

    //then
    verify(ui).showRemoveButton()
  }

  @Test
  fun `the remove button should be hidden when the sheet is opened for new entry`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController(OpenAs.New(patientUuid))

    //then
    verify(ui).hideRemoveButton()
  }

  @Test
  fun `when sheet is opened to edit prescription then the drug name and dosage should be pre-filled`() {
    //given
    val prescription = TestData.prescription(uuid = prescriptionUuid)
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(prescription))

    //when
    setupController(OpenAs.Update(prescriptionUuid))

    //then
    verify(ui).setMedicineName(prescription.name)
    verify(ui).setDosage(prescription.dosage)
  }

  @Test
  fun `when sheet is opened in edit mode and save is clicked after making changes, then the prescription should be updated`() {
    //given
    val prescribedDrug = TestData.prescription(uuid = prescriptionUuid, name = "Atnlol", dosage = "20mg")
    val updatedPrescribedDrug = prescribedDrug.copy(name = "Atenolol", dosage = "5mg")

    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(prescribedDrug))
    whenever(prescriptionRepository.updatePrescription(updatedPrescribedDrug)).thenReturn(Completable.complete())

    //when
    setupController(OpenAs.Update(prescriptionUuid))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Atenolol"))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("5mg"))
    uiEvents.onNext(SaveCustomPrescriptionClicked)

    //then
    verify(prescriptionRepository).updatePrescription(updatedPrescribedDrug)
    verify(prescriptionRepository, never()).savePrescription(any(), any(), any())
    verify(ui).finish()
  }

  @Test
  fun `when remove is clicked, then show confirmation dialog`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController(OpenAs.Update(prescriptionUuid))
    uiEvents.onNext(RemoveCustomPrescriptionClicked)

    //then
    verify(ui).showConfirmRemoveMedicineDialog(prescriptionUuid)
  }

  @Test
  fun `when prescription is deleted then close the sheet`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(TestData.prescription(uuid = prescriptionUuid, isDeleted = true)))

    //when
    setupController(OpenAs.Update(prescriptionUuid))

    //then
    verify(ui).finish()
  }

  private fun setupController(openAs: OpenAs) {
    controller = CustomPrescriptionEntryController(prescriptionRepository, openAs)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    val uiRenderer = CustomPrescriptionEntryUiRenderer(ui)
    val effectHandler = CustomPrescriptionEntryEffectHandler(
        uiActions,
        TrampolineSchedulersProvider(),
        userSession,
        facilityRepository,
        prescriptionRepository
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

    uiEvents.onNext(ScreenCreated())
  }
}
