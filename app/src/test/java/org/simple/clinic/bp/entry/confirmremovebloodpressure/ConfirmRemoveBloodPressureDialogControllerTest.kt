package org.simple.clinic.bp.entry.confirmremovebloodpressure

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
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

class ConfirmRemoveBloodPressureDialogControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  lateinit var bloodPressureRepository: BloodPressureRepository
  lateinit var controller: ConfirmRemoveBloodPressureDialogController
  lateinit var dialog: ConfirmRemoveBloodPressureDialog

  val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    bloodPressureRepository = mock()
    dialog = mock()

    controller = ConfirmRemoveBloodPressureDialogController(bloodPressureRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }

  @Test
  fun `when remove is clicked, the blood pressure must be marked as deleted and the dialog should be dismissed`() {
    val bloodPressure = PatientMocker.bp()
    val markBloodPressureDeletedCompletable = Completable.complete()
    whenever(bloodPressureRepository.measurement(bloodPressure.uuid)).thenReturn(Observable.just(bloodPressure))
    whenever(bloodPressureRepository.markBloodPressureAsDeleted(bloodPressure)).thenReturn(markBloodPressureDeletedCompletable)

    uiEvents.onNext(ConfirmRemoveBloodPressureDialogCreated(bloodPressureMeasurementUuid = bloodPressure.uuid))
    uiEvents.onNext(ConfirmRemoveBloodPressureDialogRemoveClicked)

    markBloodPressureDeletedCompletable.test().assertComplete()
    verify(dialog).dismiss()
  }
}
