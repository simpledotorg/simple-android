package org.simple.clinic.bp.entry.confirmremovebloodpressure

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class ConfirmRemoveBloodPressureDialogControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val dialog = mock<ConfirmRemoveBloodPressureDialog>()
  lateinit var controller: ConfirmRemoveBloodPressureDialogController

  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = ConfirmRemoveBloodPressureDialogController(bloodPressureRepository, patientRepository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }

  @Test
  fun `when remove is clicked, the blood pressure must be marked as deleted and the dialog should be dismissed`() {
    val patientUuid = UUID.fromString("268b4091-fb16-4472-a466-baf60c72b895")
    val bloodPressure = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("9fe5c4c8-f677-4a00-b621-19bd4503e334"),
        patientUuid = patientUuid
    )
    val markBloodPressureDeletedCompletable = Completable.complete()
    val updatePatientRecordedAtCompletable = Completable.complete()
    whenever(bloodPressureRepository.measurement(bloodPressure.uuid)).doReturn(Observable.just(bloodPressure))
    whenever(bloodPressureRepository.markBloodPressureAsDeleted(bloodPressure)).doReturn(markBloodPressureDeletedCompletable)
    whenever(patientRepository.updateRecordedAt(patientUuid)).doReturn(updatePatientRecordedAtCompletable)

    uiEvents.onNext(ConfirmRemoveBloodPressureDialogCreated(bloodPressureMeasurementUuid = bloodPressure.uuid))
    uiEvents.onNext(ConfirmRemoveBloodPressureDialogRemoveClicked)

    markBloodPressureDeletedCompletable.test().assertComplete()
    updatePatientRecordedAtCompletable.test().assertComplete()
    verify(dialog).dismiss()
  }
}
