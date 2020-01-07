package org.simple.clinic.summary.bloodpressures

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

// TODO(vs): 2020-01-06 Replace with typealiases specific to this screen once the refactor is over
typealias Ui = org.simple.clinic.summary.Ui
typealias UiChange = org.simple.clinic.summary.UiChange

class BloodPressureSummaryViewController(
    private val patientUuid: UUID,
    private val config: PatientSummaryConfig,
    private val repository: BloodPressureRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .switchMap { repository.newestMeasurementsForPatient(patientUuid, config.numberOfBpsToDisplay) }
        .map { { ui: Ui -> ui.bloodPressureSummaryUi().populateBloodPressures(it) } }
  }
}
