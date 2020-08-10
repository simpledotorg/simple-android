package org.simple.clinic.bp.entry.confirmremovebloodpressure

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.spotify.mobius.Init
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.mobius.first
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class ConfirmRemoveBloodPressureDialogLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<ConfirmRemoveBloodPressureDialogUiActions>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var testFixture: MobiusTestFixture<ConfirmRemoveBloodPressureModel, ConfirmRemoveBloodPressureEvent, ConfirmRemoveBloodPressureEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when remove is clicked, the blood pressure must be marked as deleted and the dialog should be dismissed`() {
    // given
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

    // when
    setupController(bloodPressure.uuid)
    uiEvents.onNext(ConfirmRemoveBloodPressureDialogRemoveClicked)

    // then
    markBloodPressureDeletedCompletable.test().assertComplete()
    updatePatientRecordedAtCompletable.test().assertComplete()
    verify(uiActions).closeDialog()
    verifyNoMoreInteractions(uiActions)
  }

  private fun setupController(bloodPressureMeasurementUuid: UUID) {
    val effectHandler = ConfirmRemoveBloodPressureEffectHandler(
        bloodPressureRepository = bloodPressureRepository,
        patientRepository = patientRepository,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = ConfirmRemoveBloodPressureModel.create(bloodPressureMeasurementUuid),
        init = Init { first(it) },
        update = ConfirmRemoveBloodPressureUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = { /* nothing do here*/ }
    )

    testFixture.start()
  }
}
