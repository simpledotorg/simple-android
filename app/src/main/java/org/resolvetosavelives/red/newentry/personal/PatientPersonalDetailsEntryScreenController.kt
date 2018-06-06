package org.resolvetosavelives.red.newentry.personal

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.search.OngoingPatientEntry
import org.resolvetosavelives.red.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientPersonalDetailsEntryScreen
private typealias UiChange = (Ui) -> Unit

class PatientPersonalDetailsEntryScreenController @Inject constructor(
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
        .flatMap { Observable.just { ui: Ui -> ui.showKeyboardOnFullnameField() } }
  }

  private fun preFills(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType(ScreenCreated::class.java)
        .flatMapSingle { repository.ongoingEntry() }
        .filter { entry -> entry.personalDetails != null }
        .map { entry -> entry.personalDetails }
        .flatMap { personalDetails -> Observable.just { ui: Ui -> ui.preFill(personalDetails) } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val fullNameChanges = events
        .ofType(PatientFullNameTextChanged::class.java)
        .map(PatientFullNameTextChanged::fullName)

    val dateOfBirthChanges = events
        .ofType(PatientDateOfBirthTextChanged::class.java)
        .map(PatientDateOfBirthTextChanged::dateOfBirth)

    val ageChanges = events
        .ofType(PatientAgeTextChanged::class.java)
        .map(PatientAgeTextChanged::age)

    val genderChanges = events
        .ofType(PatientGenderChanged::class.java)
        .map(PatientGenderChanged::gender)

    return events
        .ofType(PatientPersonalDetailsProceedClicked::class.java)
        .flatMapSingle { repository.ongoingEntry() }
        .withLatestFrom(fullNameChanges, dateOfBirthChanges, ageChanges, genderChanges,
            { entry, name, dob, age, gender -> entry to OngoingPatientEntry.PersonalDetails(name, dob, age, gender) })
        .take(1)
        .map { (entry, updatedPersonalDetails) -> entry.copy(personalDetails = updatedPersonalDetails) }
        .flatMapCompletable { updatedEntry -> repository.saveOngoingEntry(updatedEntry) }
        .andThen(Observable.just({ ui: Ui -> ui.openAddressEntryScreen() }))
  }
}
