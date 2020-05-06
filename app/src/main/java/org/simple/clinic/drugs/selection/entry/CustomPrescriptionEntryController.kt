package org.simple.clinic.drugs.selection.entry

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.widgets.UiEvent

private typealias Ui = CustomPrescriptionEntryUi
private typealias UiChange = (Ui) -> Unit

const val DOSAGE_PLACEHOLDER = "mg"

class CustomPrescriptionEntryController @AssistedInject constructor(
    private val prescriptionRepository: PrescriptionRepository,
    @Assisted private val openAs: OpenAs
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(openAs: OpenAs): CustomPrescriptionEntryController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
