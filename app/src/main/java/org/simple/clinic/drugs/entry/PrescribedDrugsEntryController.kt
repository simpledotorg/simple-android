package org.simple.clinic.drugs.entry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PrescribedDrugsEntryScreen
typealias UiChange = (Ui) -> Unit

class PrescribedDrugsEntryController @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository
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
        .take(1)

    // Flat-mapping with patientUuid is not required, but is helpful in
    // tests to block execution until an event is emitted. In this case,
    // PrescribedDrugsEntryScreenCreated is that event.
    val protocolDrugs = patientUuid
        .flatMap { protocolRepository.currentProtocol() }
        .map { it.drugs }

    val prescribedDrugs = patientUuid
        .flatMap { prescriptionRepository.newestPrescriptionsForPatient(it) }

    return Observables
        .combineLatest(protocolDrugs, prescribedDrugs)
        .map { (protocolDrugs, prescribedDrugs) ->
          val prescribedDrugsNamesAndDosages = prescribedDrugs
              .filter { it.dosage.isNullOrBlank().not() }
              .map { it.name to it.dosage!! }
              .toHashSet()

          val protocolDrugSelectionItems = protocolDrugs
              .mapIndexed { index, drug ->
                val dosage1 = drug.dosages[0]
                val dosage2 = drug.dosages[1]
                val isDosage1Selected = prescribedDrugsNamesAndDosages.contains(drug.name to dosage1)
                val isDosage2Selected = prescribedDrugsNamesAndDosages.contains(drug.name to dosage2)

                ProtocolDrugSelectionItem(
                    id = index,
                    name = drug.name,
                    option1 = ProtocolDrugSelectionItem.DosageOption(dosage = dosage1, isSelected = isDosage1Selected),
                    option2 = ProtocolDrugSelectionItem.DosageOption(dosage = dosage2, isSelected = isDosage2Selected))
              }

          val protocolDrugsNamesAndDosages: HashSet<Pair<String, String>> = protocolDrugs
              .flatMap { drug ->
                drug.dosages.map { dosage ->
                  drug.name to dosage
                }
              }
              .toHashSet()

          val customPrescribedDrugItems = prescribedDrugs
              .filter { !protocolDrugsNamesAndDosages.contains(it.name to it.dosage) }
              .map { CustomPrescribedDrugItem(adapterId = it.uuid, name = it.name, dosage = it.dosage) }

          protocolDrugSelectionItems + customPrescribedDrugItems
        }
        .map { { ui: Ui -> ui.populateDrugsList(it) } }
  }
}
