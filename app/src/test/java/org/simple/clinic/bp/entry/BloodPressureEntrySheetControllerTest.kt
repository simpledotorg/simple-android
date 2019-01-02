package org.simple.clinic.bp.entry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.bp.BloodPressureConfig
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureEntrySheetControllerTest {

  private val sheet = mock<BloodPressureEntrySheet>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientUuid = UUID.randomUUID()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val configEmitter = PublishSubject.create<BloodPressureConfig>()
  private lateinit var controller: BloodPressureEntrySheetController

  @Before
  fun setUp() {
    controller = BloodPressureEntrySheetController(bloodPressureRepository, configEmitter.firstOrError())

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(sheet) }
  }

  @Test
  @Parameters(value = [
    "90|true",
    "120|true",
    "300|true",
    "66|false",
    "44|false"
  ])
  fun `when valid systolic value is entered, move cursor to diastolic field`(sampleSystolicBp: String, shouldMove: Boolean) {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged(sampleSystolicBp))

    when (shouldMove) {
      true -> verify(sheet).changeFocusToDiastolic()
      false -> verify(sheet, never()).changeFocusToDiastolic()
    }
  }

  @Test
  @Parameters(value = [
    "170, 17",
    "17, 1",
    "1,",
    ","])
  fun `when backspace is pressed in diastolic field and it's text is empty, move cursor to systolic and delete last digit`(
      existingSystolic: String,
      systolicAfterBackspace: String
  ) {
    uiEvents.onNext(BloodPressureSystolicTextChanged(existingSystolic))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("142"))

    uiEvents.onNext(BloodPressureDiastolicBackspaceClicked)
    uiEvents.onNext(BloodPressureDiastolicTextChanged("14"))

    uiEvents.onNext(BloodPressureDiastolicBackspaceClicked)
    uiEvents.onNext(BloodPressureDiastolicTextChanged("1"))

    uiEvents.onNext(BloodPressureDiastolicBackspaceClicked)
    uiEvents.onNext(BloodPressureDiastolicTextChanged(""))

    uiEvents.onNext(BloodPressureDiastolicBackspaceClicked)

    verify(sheet).changeFocusToSystolic()
    if (systolicAfterBackspace.isNotEmpty()) {
      verify(sheet).setSystolic(systolicAfterBackspace)
    }
  }

  @Test
  fun `when systolic is less than diastolic, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("90"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("140"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showSystolicLessThanDiastolicError()
  }

  @Test
  fun `when systolic is less than minimum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("55"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("55"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showSystolicLowError()
  }

  @Test
  fun `when systolic is more than maximum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("333"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("88"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showSystolicHighError()
  }

  @Test
  fun `when diastolic is less than minimum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("110"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("33"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showDiastolicLowError()
  }

  @Test
  fun `when diastolic is more than maximum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("233"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("190"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showDiastolicHighError()
  }

  @Test
  fun `when systolic is empty, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged(""))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("190"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showSystolicEmptyError()
  }

  @Test
  fun `when diastolic is empty, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("120"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged(""))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showDiastolicEmptyError()
  }

  @Test
  fun `when systolic or diastolic values change, hide the error message`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("12"))
    uiEvents.onNext(BloodPressureSystolicTextChanged("120"))
    uiEvents.onNext(BloodPressureSystolicTextChanged("130"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("90"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("99"))

    verify(sheet, times(5)).hideErrorMessage()
  }

  @Test
  @Parameters(value = [
    ",",
    "1,1"
  ])
  fun `when save is clicked but input is invalid then blood pressure measurement should not be saved`(
      systolic: String,
      diastolic: String
  ) {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged(systolic))
    uiEvents.onNext(BloodPressureDiastolicTextChanged(diastolic))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet, never()).setBPSavedResultAndFinish()
  }

  @Test
  @Parameters(method = "params for checking valid input")
  fun `when save is clicked and input is valid then blood pressure measurement should be saved`(openAs: OpenAs) {
    var alreadyPresentBp: BloodPressureMeasurement? = null

    if (openAs is OpenAs.Update) {
      val bpUuid = openAs.bpUuid

      alreadyPresentBp = PatientMocker.bp(uuid = bpUuid, patientUuid = patientUuid, systolic = 120, diastolic = 80)
      whenever(bloodPressureRepository.measurement(bpUuid)).thenReturn(Single.just(alreadyPresentBp))
      whenever(bloodPressureRepository.updateMeasurement(any())).thenReturn(Completable.complete())
      whenever(bloodPressureRepository.deletedMeasurementAsStream(any())).thenReturn(Observable.never())

    } else if (openAs is OpenAs.New) {
      whenever(bloodPressureRepository.saveMeasurement(openAs.patientUuid, 142, 80)).thenReturn(Single.just(PatientMocker.bp()))
    }

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))
    uiEvents.onNext(BloodPressureSystolicTextChanged("142"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("80"))
    uiEvents.onNext(BloodPressureSaveClicked())
    uiEvents.onNext(BloodPressureSaveClicked())
    uiEvents.onNext(BloodPressureSaveClicked())

    if (openAs is OpenAs.New) {
      verify(bloodPressureRepository).saveMeasurement(openAs.patientUuid, 142, 80)
      verify(bloodPressureRepository, never()).updateMeasurement(any())
    } else {
      verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
      verify(bloodPressureRepository).updateMeasurement(alreadyPresentBp!!.copy(systolic = 142, diastolic = 80))
    }
    verify(sheet).setBPSavedResultAndFinish()
  }

  @Suppress("Unused")
  private fun `params for checking valid input`(): List<Any> {
    return listOf(OpenAs.New(patientUuid), OpenAs.Update(UUID.randomUUID()))
  }

  @Test
  @Parameters(method = "params for prefilling bp measurements")
  fun `when screen is opened to update a blood pressure, the blood pressure must be prefilled`(
      openAs: OpenAs,
      bloodPressureMeasurement: BloodPressureMeasurement?
  ) {
    if (openAs is OpenAs.Update) {
      whenever(bloodPressureRepository.measurement(any())).thenReturn(Single.just(bloodPressureMeasurement!!))
      whenever(bloodPressureRepository.deletedMeasurementAsStream(any())).thenReturn(Observable.never())
    }

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))

    if (openAs is OpenAs.Update) {
      val verify = verify(sheet)
      verify.setSystolic(bloodPressureMeasurement!!.systolic.toString())
      verify.setDiastolic(bloodPressureMeasurement.diastolic.toString())
    } else {
      val verify = verify(sheet, never())
      verify.setSystolic(any())
      verify.setDiastolic(any())
    }
  }

  @Suppress("Unused")
  private fun `params for prefilling bp measurements`(): List<List<Any?>> {
    val bpUuid = UUID.randomUUID()
    return listOf(
        listOf(OpenAs.New(patientUuid), null),
        listOf(OpenAs.Update(bpUuid), PatientMocker.bp(uuid = bpUuid, patientUuid = patientUuid))
    )
  }

  /**
   * **Note:** When removing the feature flag, this test should **NOT** be removed, but modified
   * to test that the button is shown only when the OpenAs is [OpenAs.Update].
   **/
  @Test
  @Parameters(method = "params for enabling delete bp feature")
  fun `when the delete blood pressure feature flag is enabled and the sheet is opened for update, the remove BP button must be shown`(
      featureEnabled: Boolean,
      openAs: OpenAs,
      shouldShowRemoveBpButton: Boolean
  ) {
    configEmitter.onNext(BloodPressureConfig(deleteBloodPressureFeatureEnabled = featureEnabled))
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Single.just(PatientMocker.bp()))
    whenever(bloodPressureRepository.deletedMeasurementAsStream(any())).thenReturn(Observable.never())

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))

    if (shouldShowRemoveBpButton) {
      verify(sheet).showRemoveBpButton()
    } else {
      verify(sheet).hideRemoveBpButton()
    }
  }

  @Suppress("Unused")
  private fun `params for enabling delete bp feature`(): List<List<Any>> {
    return listOf(
        listOf(true, OpenAs.New(UUID.randomUUID()), false),
        listOf(true, OpenAs.Update(UUID.randomUUID()), true),
        listOf(false, OpenAs.New(UUID.randomUUID()), false),
        listOf(false, OpenAs.Update(UUID.randomUUID()), false))
  }

  @Test
  @Parameters(method = "params for setting the title of the sheet")
  fun `the correct title should be shown when the sheet is opened`(
      openAs: OpenAs,
      showEntryTitle: Boolean
  ) {
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Single.just(PatientMocker.bp()))
    whenever(bloodPressureRepository.deletedMeasurementAsStream(any())).thenReturn(Observable.never())

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))

    if (showEntryTitle) {
      verify(sheet).showEnterNewBloodPressureTitle()
    } else {
      verify(sheet).showEditBloodPressureTitle()
    }
  }

  @Suppress("Unused")
  private fun `params for setting the title of the sheet`(): List<List<Any>> {
    return listOf(
        listOf(OpenAs.New(UUID.randomUUID()), true),
        listOf(OpenAs.Update(UUID.randomUUID()), false))
  }

  @Test
  fun `when the remove button is clicked, the confirmation alert must be shown`() {
    val bloodPressure = PatientMocker.bp()
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Single.just(bloodPressure))
    whenever(bloodPressureRepository.deletedMeasurementAsStream(any())).thenReturn(Observable.never())

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs = OpenAs.Update(bloodPressure.uuid)))
    uiEvents.onNext(BloodPressureRemoveClicked)

    verify(sheet).showConfirmRemoveBloodPressureDialog(bloodPressure.uuid)
  }

  @Test
  fun `when a blood pressure being edited is removed, the sheet should be closed`() {
    val bloodPressure = PatientMocker.bp()
    whenever(bloodPressureRepository.measurement(bloodPressure.uuid)).thenReturn(Single.just(bloodPressure))
    whenever(bloodPressureRepository.deletedMeasurementAsStream(bloodPressure.uuid))
        .thenReturn(Observable.just(bloodPressure.copy(deletedAt = Instant.now())))

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs = OpenAs.Update(bpUuid = bloodPressure.uuid)))

    verify(sheet).setBPSavedResultAndFinish()
  }
}
