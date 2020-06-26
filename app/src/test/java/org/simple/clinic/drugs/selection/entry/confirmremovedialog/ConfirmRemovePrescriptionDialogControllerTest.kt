package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.bp.entry.confirmremovebloodpressure.RemovePrescriptionClicked
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class ConfirmRemovePrescriptionDialogControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val dialog = mock<ConfirmRemovePrescriptionDialog>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val prescribedDrugUuid = UUID.fromString("fe94ba47-4d34-476c-809f-c2adfc11914a")

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
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
    verify(dialog).dismiss()
    verifyNoMoreInteractions(dialog)
  }

  private fun setupController() {
    val controller = ConfirmRemovePrescriptionDialogController(prescriptionRepository, prescribedDrugUuid)
    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }
}
