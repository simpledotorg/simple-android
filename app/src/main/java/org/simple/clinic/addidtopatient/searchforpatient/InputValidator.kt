package org.simple.clinic.addidtopatient.searchforpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.addidtopatient.searchforpatient.SearchQueryValidationResult.Invalid
import org.simple.clinic.addidtopatient.searchforpatient.SearchQueryValidationResult.Valid
import org.simple.clinic.widgets.UiEvent

class InputValidator : ObservableTransformer<UiEvent, UiEvent> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiEvent> {
    val textChanges = events
        .ofType<SearchQueryTextChanged>()
        .map { it.text.trim() }

    val validationResults = events
        .ofType<SearchClicked>()
        .withLatestFrom(textChanges) { _, text -> text }
        .map(this::validateInput)

    return events.mergeWith(validationResults)
  }

  private fun validateInput(inputText: String): SearchQueryValidationResult {
    return if (inputText.isBlank()) {
      Invalid(listOf(AddIdToPatientSearchValidationError.INPUT_EMPTY))
    } else {
      Valid(inputText)
    }
  }
}
