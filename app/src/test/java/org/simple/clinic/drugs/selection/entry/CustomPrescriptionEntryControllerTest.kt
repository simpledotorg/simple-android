package org.simple.clinic.drugs.selection.entry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class CustomPrescriptionEntryControllerTest {

  private val sheet = mock<CustomPrescriptionEntrySheet>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientUuid = UUID.randomUUID()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: CustomPrescriptionEntryController

  @Before
  fun setUp() {
    controller = CustomPrescriptionEntryController(prescriptionRepository)

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

    uiEvents.onNext(CustomPrescriptionSheetCreated(patientUuid))
    uiEvents.onNext(CustomPrescriptionDrugNameTextChanged("Amlodipine"))
    uiEvents.onNext(CustomPrescriptionDrugDosageTextChanged(dosage))
    uiEvents.onNext(SaveCustomPrescriptionClicked())

    verify(prescriptionRepository).savePrescription(patientUuid, "Amlodipine", dosage.nullIfBlank(), rxNormCode = null, isProtocolDrug = false)
    verify(sheet).finish()
  }
}
