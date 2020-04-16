package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemovePrescriptionDialogCreated
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
  lateinit var controller: ConfirmRemovePrescriptionDialogController

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val prescribedDrugUuid = UUID.randomUUID()

  @Before
  fun setUp() {
    controller = ConfirmRemovePrescriptionDialogController(prescriptionRepository)
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }

  @Test
  fun `when remove is clicked, then delete prescription`() {
    whenever(prescriptionRepository.softDeletePrescription(prescribedDrugUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(ConfirmRemovePrescriptionDialogCreated(prescribedDrugUuid))
    uiEvents.onNext(RemovePrescriptionClicked)

    verify(prescriptionRepository).softDeletePrescription(prescribedDrugUuid)
    verify(dialog).dismiss()
  }

}
