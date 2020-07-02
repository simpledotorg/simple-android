package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

typealias Ui = UiActions
typealias UiChange = (Ui) -> Unit

class ConfirmRemovePrescriptionDialogController @AssistedInject constructor(
    private val prescriptionRepository: PrescriptionRepository,
    @Assisted private val prescribedDrugUuid: UUID
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(prescribedDrugUuid: UUID): ConfirmRemovePrescriptionDialogController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()
    return Observable.never()
  }
}
