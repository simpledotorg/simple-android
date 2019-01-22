package org.simple.clinic.drugs.selectionv2.entry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class CustomPrescriptionEntryControllerv2Test {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val sheet = mock<CustomPrescriptionEntrySheetv2>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientUuid = UUID.randomUUID()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: CustomPrescriptionEntryControllerv2

  @Before
  fun setUp() {
    controller = CustomPrescriptionEntryControllerv2(prescriptionRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(sheet) }
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
  fun `when save is clicked then a new prescription should be saved`(dosage: String) {
    whenever(prescriptionRepository.savePrescription(patientUuid, "Amlodipine", dosage.nullIfBlank(), rxNormCode = null, isProtocolDrug = false))
        .thenReturn(Completable.complete())

    uiEvents.onNext(CustomPrescriptionSheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Amlodipine"))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(dosage))
    uiEvents.onNext(SaveCustomPrescriptionClicked())

    verify(prescriptionRepository).savePrescription(patientUuid, "Amlodipine", dosage.nullIfBlank(), rxNormCode = null, isProtocolDrug = false)
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
}
