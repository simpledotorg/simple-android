package org.simple.clinic.bp.entry.confirmremovebloodpressure

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.ConfirmRemoveBloodPressureDialog
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

class ConfirmRemoveBloodPressureDialogControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val dialog = mock<ConfirmRemoveBloodPressureDialog>()
  lateinit var controller: ConfirmRemoveBloodPressureDialogController

  val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = ConfirmRemoveBloodPressureDialogController(bloodPressureRepository, patientRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }

  @Test
  fun `when remove is clicked, the blood pressure must be marked as deleted and the dialog should be dismissed`() {
    val bloodPressure = PatientMocker.bp()
    val markBloodPressureDeletedCompletable = Completable.complete()
    val updatePatientRecordedAtCompletable = Completable.complete()
    whenever(bloodPressureRepository.measurement(bloodPressure.uuid)).thenReturn(Observable.just(bloodPressure))
    whenever(bloodPressureRepository.markBloodPressureAsDeleted(bloodPressure)).thenReturn(markBloodPressureDeletedCompletable)
    whenever(patientRepository.updateRecordedAt(any())).thenReturn(updatePatientRecordedAtCompletable)

    uiEvents.onNext(ConfirmRemoveBloodPressureDialogCreated(bloodPressureMeasurementUuid = bloodPressure.uuid))
    uiEvents.onNext(ConfirmRemoveBloodPressureDialogRemoveClicked)

    markBloodPressureDeletedCompletable.test().assertComplete()
    updatePatientRecordedAtCompletable.test().assertComplete()
    verify(dialog).dismiss()
  }
}
