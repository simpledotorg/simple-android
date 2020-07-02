package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.spotify.mobius.Init
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.mobius.first
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class ConfirmRemovePrescriptionDialogControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val uiActions = mock<UiActions>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val prescribedDrugUuid = UUID.fromString("fe94ba47-4d34-476c-809f-c2adfc11914a")

  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<ConfirmRemovePrescriptionDialogModel, ConfirmRemovePrescriptionDialogEvent, ConfirmRemovePrescriptionDialogEffect>

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    testFixture.dispose()
  }

  @Test
  fun `when remove is clicked, then delete prescription`() {
    // given
    whenever(prescriptionRepository.softDeletePrescription(prescribedDrugUuid)).thenReturn(Completable.complete())

    // when
    setupController()
    uiEvents.onNext(RemovePrescriptionClicked)

    // then
    verify(prescriptionRepository).softDeletePrescription(prescribedDrugUuid)
    verify(uiActions).closeDialog()
    verifyNoMoreInteractions(uiActions)
  }

  private fun setupController() {
    val controller = ConfirmRemovePrescriptionDialogController(prescriptionRepository, prescribedDrugUuid)
    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(uiActions) }

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = ConfirmRemovePrescriptionDialogModel.create(prescribedDrugUuid),
        init = Init { first(it) },
        update = ConfirmRemovePrescriptionDialogUpdate(),
        effectHandler = ConfirmRemovePrescriptionDialogEffectHandler(
            schedulersProvider = TestSchedulersProvider.trampoline(),
            prescriptionRepository = prescriptionRepository,
            uiActions = uiActions
        ).build(),
        modelUpdateListener = { }
    )

    testFixture.start()
  }
}
