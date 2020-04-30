package org.simple.clinic.summary.medicalhistory

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

typealias Ui = MedicalHistorySummaryUi

typealias UiChange = (Ui) -> Unit

class MedicalHistorySummaryUiController @AssistedInject constructor(
    @Assisted private val patientUuid: UUID
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: UUID): MedicalHistorySummaryUiController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
