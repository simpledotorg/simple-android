package org.resolvetosavelives.red.newentry.personal

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

typealias UiChange = (PatientPersonalDetailsEntryScreen) -> Unit

class PatientPersonalDetailsEntryScreenController @Inject constructor(
    private val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        keyboardCalls(),
        submitClicks(replayedEvents))
  }

  private fun keyboardCalls(): Observable<UiChange> {
    return Observable.just({ screen: PatientPersonalDetailsEntryScreen -> screen.showKeyboardOnFullnameField() })
  }

  private fun submitClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val fullNameChanges = events
        .ofType(PatientFullNameTextChanged::class.java)
        .map { event -> event.fullName }

    return events
        .ofType(PatientPersonalDetailsProceedClicked::class.java)
        .flatMapSingle { repository.ongoingEntry() }
        .withLatestFrom(fullNameChanges, BiFunction { entry: OngoingPatientEntry, fullName: String -> Pair(entry, fullName) })
        .take(1)
        .map { (entry, fullName) -> entry.copy(personalDetails = OngoingPatientEntry.PersonalDetails(fullName)) }
        .flatMapCompletable { updatedEntry -> repository.save(updatedEntry) }
        .andThen(Observable.just({ screen: PatientPersonalDetailsEntryScreen -> screen.openAddressEntryScreen() }))
  }
}
