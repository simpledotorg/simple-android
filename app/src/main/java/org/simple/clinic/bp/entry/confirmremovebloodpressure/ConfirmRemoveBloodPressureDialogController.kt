package org.simple.clinic.bp.entry.confirmremovebloodpressure

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

typealias Ui = ConfirmRemoveBloodPressureDialogUi
typealias UiChange = (Ui) -> Unit

class ConfirmRemoveBloodPressureDialogController @AssistedInject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val patientRepository: PatientRepository,
    @Assisted private val bloodPressureMeasurementUuid: UUID
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(bloodPressureMeasurementUuid: UUID): ConfirmRemoveBloodPressureDialogController
  }

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
