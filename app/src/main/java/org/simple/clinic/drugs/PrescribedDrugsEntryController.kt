package org.simple.clinic.drugs

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.widgets.UiEvent
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

typealias Ui = PrescribedDrugsEntryScreen
typealias UiChange = (Ui) -> Unit

class PrescribedDrugsEntryController @Inject constructor(
    private val protocolRepository: ProtocolRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    return Observable.mergeArray(
        populateDrugsList(replayedEvents))
  }

  private fun populateDrugsList(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuid = events
        .ofType<PrescribedDrugsEntryScreenCreated>()
        .map { it.patientUuid }
        .doOnNext { Timber.i("Patient uuid: $it") }

    val protocolDrugs = protocolRepository
        .currentProtocol()
        .map { it.drugs }

    return Observables.combineLatest(protocolDrugs, patientUuid)
        .map {
          listOf(
              ProtocolDrugSelectionItem(UUID.fromString("fdfaf8cf-e40a-418b-827a-71d3e7712495"), "Amlodipine", "5mg", "10mg"),
              ProtocolDrugSelectionItem(UUID.fromString("fdfaf8cf-e40a-418b-827a-71d3e7712495"), "Telmisartan", "40mg", "80mg"),
              ProtocolDrugSelectionItem(UUID.fromString("fdfaf8cf-e40a-418b-827a-71d3e7712495"), "Chlorthalidone", "12.5mg", "25mg"))
        }
        .doOnNext { Timber.i("Created drugs: $it") }
        .map { { ui: Ui -> ui.populateDrugsList(it) } }
  }
}
