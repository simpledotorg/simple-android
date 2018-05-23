package org.resolvetosavelives.red.newentry.address

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientAddressEntryScreen
private typealias UiChange = (Ui) -> Unit

class PatientAddressEntryScreenController @Inject constructor(
    private val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        keyboardCalls(replayedEvents),
        preFills(replayedEvents),
        saveAndProceeds(replayedEvents))
  }

  private fun keyboardCalls(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType(ScreenCreated::class.java)
        .flatMap { Observable.just { ui: Ui -> ui.showKeyboardOnColonyField() } }
  }

  private fun preFills(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType(ScreenCreated::class.java)
        .flatMapSingle { repository.ongoingEntry() }
        .filter { entry -> entry.address != null }
        .map { entry -> entry.address }
        .flatMap { address -> Observable.just { ui: Ui -> ui.preFill(address) } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val villageOrColonyChanges = events
        .ofType(PatientAddressColonyOrVillageTextChanged::class.java)
        .map(PatientAddressColonyOrVillageTextChanged::colonyOrVillage)

    val districtChanges = events
        .ofType(PatientAddressDistrictTextChanged::class.java)
        .map(PatientAddressDistrictTextChanged::district)

    val stateChanges = events
        .ofType(PatientAddressStateTextChanged::class.java)
        .map(PatientAddressStateTextChanged::state)

    return events
        .ofType(PatientAddressEntryProceedClicked::class.java)
        .flatMapSingle { repository.ongoingEntry() }
        .withLatestFrom(villageOrColonyChanges, districtChanges, stateChanges,
            { entry, villageOrColony, district, state -> entry to OngoingPatientEntry.Address(villageOrColony, district, state) })
        .take(1)
        .map { (entry, updatedAddress) -> entry.copy(address = updatedAddress) }
        .flatMapCompletable { updatedEntry -> repository.saveOngoingEntry(updatedEntry) }
        .andThen(Observable.just({ ui: Ui -> ui.openPatientPhoneEntryScreen() }))
  }
}
