package org.simple.clinic.bp.entry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class BloodPressureEntrySheetControllerTest {

  private val sheet = mock<BloodPressureEntrySheet>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientUuid = UUID.randomUUID()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: BloodPressureEntrySheetController

  @Before
  fun setUp() {
    controller = BloodPressureEntrySheetController(bloodPressureRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(sheet) }
  }

  @Test
  fun `when systolic is less than diastolic, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(patientUuid))
    uiEvents.onNext(BloodPressureSystolicTextChanged("90"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("140"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showSystolicLessThanDiastolicError()
  }

  @Test
  fun `when systolic is less than minimum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(patientUuid))
    uiEvents.onNext(BloodPressureSystolicTextChanged("55"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("55"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showSystolicLowError()
  }

  @Test
  fun `when systolic is more than maximum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(patientUuid))
    uiEvents.onNext(BloodPressureSystolicTextChanged("333"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("88"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showSystolicHighError()
  }

  @Test
  fun `when diastolic is less than minimum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(patientUuid))
    uiEvents.onNext(BloodPressureSystolicTextChanged("110"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("33"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showDiastolicLowError()
  }

  @Test
  fun `when diastolic is more than maximum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(patientUuid))
    uiEvents.onNext(BloodPressureSystolicTextChanged("233"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("190"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet).showDiastolicHighError()
  }

  @Test
  fun `when systolic or diastolic values change, hide the error message`() {
    uiEvents.onNext(BloodPressureSystolicTextChanged("12"))
    uiEvents.onNext(BloodPressureSystolicTextChanged("120"))
    uiEvents.onNext(BloodPressureSystolicTextChanged("130"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("90"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("99"))

    verify(sheet, times(5)).hideErrorMessage()
  }

  @Test
  fun `when save is clicked but input is invalid then blood pressure measurement should not be saved`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(patientUuid))
    uiEvents.onNext(BloodPressureSystolicTextChanged("1"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("1"))
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any())
    verify(sheet, never()).setBPSavedResultAndFinish()
  }

  @Test
  fun `when save is clicked and input is valid then blood pressure measurement should be saved`() {
    whenever(bloodPressureRepository.saveMeasurement(patientUuid, 142, 80)).thenReturn(Single.just(PatientMocker.bp()))

    uiEvents.onNext(BloodPressureEntrySheetCreated(patientUuid))
    uiEvents.onNext(BloodPressureSystolicTextChanged("142"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("80"))
    uiEvents.onNext(BloodPressureSaveClicked())
    uiEvents.onNext(BloodPressureSaveClicked())
    uiEvents.onNext(BloodPressureSaveClicked())

    verify(bloodPressureRepository, times(1)).saveMeasurement(patientUuid, 142, 80)
    verify(sheet).setBPSavedResultAndFinish()
  }
}
