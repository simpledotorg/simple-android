package org.simple.clinic.drugs.selection.entry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
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
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
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
  private val patientUuid = UUID.randomUUID()
  private val prescriptionUuid = UUID.randomUUID()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val user = PatientMocker.loggedInUser()
  private val facility = PatientMocker.facility()
  private val userSubject = PublishSubject.create<User>()

  private val controller = CustomPrescriptionEntryController(prescriptionRepository, userSession, facilityRepository)

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser()).thenReturn(userSubject)
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(sheet) }

    userSubject.onNext(user)
  }

  @Test
  fun `save should remain disabled while drug name is empty`() {
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged(""))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("A"))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Am"))

    verify(sheet).setSaveButtonEnabled(false)
    verify(sheet, times(1)).setSaveButtonEnabled(true)
  }

  @Test
  @Parameters(value = ["", "10mg"])
  fun `when sheet is opened in new mode and save is clicked then a new prescription should be saved`(dosage: String) {
    whenever(prescriptionRepository.savePrescription(
        patientUuid = patientUuid,
        name = "Amlodipine",
        dosage = dosage.nullIfBlank(),
        rxNormCode = null,
        isProtocolDrug = false,
        facility = facility
    )).thenReturn(Completable.complete())

    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Amlodipine"))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(dosage))
    uiEvents.onNext(SaveCustomPrescriptionClicked)

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
    whenever(sheet.setDrugDosageText(any())).then {
      val text = it.arguments[0] as String
      uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(text))
    }

    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(""))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("10$DOSAGE_PLACEHOLDER"))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(false))

    verify(sheet, times(1)).setDrugDosageText(eq(""))
    verify(sheet, times(1)).setDrugDosageText(eq(DOSAGE_PLACEHOLDER))
  }

  @Test
  fun `when dosage field is focused and the placeholder value is set then the cursor should be moved to the beginning`() {
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("mg"))
    uiEvents.onNext(CustomPrescriptionDrugDosageFocusChanged(true))

    verify(sheet).moveDrugDosageCursorToBeginning()
  }

  @Test
  @Parameters(method = "params for showing title")
  fun `when sheet is created then correct title should be populated`(openAs: OpenAs, showNewEntryTitle: Boolean) {
    whenever(prescriptionRepository.prescription(any())).thenReturn(Observable.never())

    uiEvents.onNext(CustomPrescriptionSheetCreated(openAs))

    if (showNewEntryTitle) {
      verify(sheet).showEnterNewPrescriptionTitle()
    } else {
      verify(sheet).showEditPrescriptionTitle()
    }
  }

  @Suppress("Unused")
  private fun `params for showing title`(): List<List<Any>> {
    return listOf(
        listOf(OpenAs.New(patientUuid), true),
        listOf(OpenAs.Update(prescriptionUuid), false)
    )
  }

  @Parameters(method = "params for showing remove button")
  @Test
  fun `the remove button should when the sheet is opened for edit`(openAs: OpenAs, showRemoveButton: Boolean) {
    whenever(prescriptionRepository.prescription(any())).thenReturn(Observable.never())

    uiEvents.onNext(CustomPrescriptionSheetCreated(openAs))

    if (showRemoveButton) {
      verify(sheet).showRemoveButton()
    } else {
      verify(sheet).hideRemoveButton()
    }
  }

  @Suppress("Unused")
  private fun `params for showing remove button`(): List<List<Any>> {
    return listOf(
        listOf(OpenAs.New(patientUuid), false),
        listOf(OpenAs.Update(prescriptionUuid), true)
    )
  }

  @Test
  fun `when sheet is opened to edit prescription then the drug name and dosage should be pre-filled`() {
    val prescription = PatientMocker.prescription(uuid = prescriptionUuid)
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(prescription))

    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))

    verify(sheet).setMedicineName(prescription.name)
    verify(sheet).setDosage(prescription.dosage)
  }

  @Test
  fun `when sheet is opened in edit mode and save is clicked after making changes, then the prescription should be updated`() {
    val prescribedDrug = PatientMocker.prescription(uuid = prescriptionUuid, name = "Atnlol", dosage = "20mg")

    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(prescribedDrug))
    whenever(prescriptionRepository.updatePrescription(any())).thenReturn(Completable.complete())

    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Atenolol"))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged("5mg"))
    uiEvents.onNext(SaveCustomPrescriptionClicked)

    verify(prescriptionRepository).updatePrescription(prescribedDrug.copy(name = "Atenolol", dosage = "5mg"))
    verify(prescriptionRepository, never()).savePrescription(any(), any(), any())
    verify(sheet).finish()
  }

  @Test
  fun `when remove is clicked, the prescription should be deleted`() {
    whenever(prescriptionRepository.prescription(any())).thenReturn(Observable.never())

    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))
    uiEvents.onNext(RemoveCustomPrescriptionClicked)

    verify(sheet).showConfirmRemoveMedicineDialog(prescriptionUuid)
  }

  @Test
  fun `when prescription is deleted then close the sheet`() {
    whenever(prescriptionRepository.prescription(prescriptionUuid)).thenReturn(Observable.just(PatientMocker.prescription(uuid = prescriptionUuid, isDeleted = true)))

    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.Update(prescriptionUuid)))

    verify(sheet).finish()
  }
}
