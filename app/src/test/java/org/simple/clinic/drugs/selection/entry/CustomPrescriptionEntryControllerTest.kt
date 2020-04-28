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
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class CustomPrescriptionEntryControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val sheet = mock<CustomPrescriptionEntrySheet>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientUuid = UUID.fromString("a90376d0-e29a-428f-80dc-bd4bdd74d9bf")
  private val prescriptionUuid = UUID.fromString("eef2b1c9-52cd-43d9-b109-b120b0e4c16c")
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val user = TestData.loggedInUser()
  private val facility = TestData.facility()
  private val userSubject = PublishSubject.create<User>()

  private val controller = CustomPrescriptionEntryController(prescriptionRepository, userSession, facilityRepository)

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser()).thenReturn(userSubject)
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))
  }

  @Test
  fun `save should remain disabled while drug name is empty`() {
    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged(""))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("A"))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Am"))

    //then
    verify(sheet).setSaveButtonEnabled(false)
    verify(sheet, times(1)).setSaveButtonEnabled(true)
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
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.New(patientUuid)))
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
    verify(sheet).finish()
  }

  @Test
  fun `placeholder value for dosage should only be shown when dosage field is focused and empty`() {
    //given
    whenever(sheet.setDrugDosageText(any())).then {
      val text = it.arguments[0] as String
      uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(text))
    }

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(""))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("10$DOSAGE_PLACEHOLDER"))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))

    //then
    verify(sheet, times(1)).setDrugDosageText(eq(""))
    verify(sheet, times(1)).setDrugDosageText(eq(DOSAGE_PLACEHOLDER))
  }

  @Test
  fun `when dosage field is focused and the placeholder value is set then the cursor should be moved to the beginning`() {
    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("mg"))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))

    //then
    verify(sheet).moveDrugDosageCursorToBeginning()
  }

  @Test
  fun `when sheet is opened for a new entry then enter new medicine title should be shown`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.New(patientUuid)))

    //then
    verify(sheet).showEnterNewPrescriptionTitle()
  }

  @Test
  fun `when sheet is opened to update a medicine then update medicine title should be shown`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))

    //then
    verify(sheet).showEditPrescriptionTitle()
  }

  @Test
  fun `the remove button should show when the sheet is opened for edit`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))

    //then
    verify(sheet).showRemoveButton()
  }

  @Test
  fun `the remove button should be hidden when the sheet is opened for new entry`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.New(patientUuid)))

    //then
    verify(sheet).hideRemoveButton()
  }

  @Test
  fun `when sheet is opened to edit prescription then the drug name and dosage should be pre-filled`() {
    //given
    val prescription = TestData.prescription(uuid = prescriptionUuid)
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(prescription))

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))

    //then
    verify(sheet).setMedicineName(prescription.name)
    verify(sheet).setDosage(prescription.dosage)
  }

  @Test
  fun `when sheet is opened in edit mode and save is clicked after making changes, then the prescription should be updated`() {
    //given
    val prescribedDrug = TestData.prescription(uuid = prescriptionUuid, name = "Atnlol", dosage = "20mg")
    val updatedPrescribedDrug = prescribedDrug.copy(name = "Atenolol", dosage = "5mg")

    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(prescribedDrug))
    whenever(prescriptionRepository.updatePrescription(updatedPrescribedDrug)).thenReturn(Completable.complete())

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Atenolol"))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("5mg"))
    uiEvents.onNext(SaveCustomPrescriptionClicked)

    //then
    verify(prescriptionRepository).updatePrescription(updatedPrescribedDrug)
    verify(prescriptionRepository, never()).savePrescription(any(), any(), any())
    verify(sheet).finish()
  }

  @Test
  fun `when remove is clicked, the prescription should be deleted`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.never())

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))
    uiEvents.onNext(RemoveCustomPrescriptionClicked)

    //then
    verify(sheet).showConfirmRemoveMedicineDialog(prescriptionUuid)
  }

  @Test
  fun `when prescription is deleted then close the sheet`() {
    //given
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(TestData.prescription(uuid = prescriptionUuid, isDeleted = true)))

    //when
    setupController()
    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))

    //then
    verify(sheet).finish()
  }

  private fun setupController() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(sheet) }

    userSubject.onNext(user)
  }
}
