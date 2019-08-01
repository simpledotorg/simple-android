package org.simple.clinic.search

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.search.SearchQueryValidationResult.Invalid
import org.simple.clinic.search.SearchQueryValidationResult.Valid
import org.simple.clinic.widgets.UiEvent

class InputValidator : ObservableTransformer<UiEvent, UiEvent> {

  override fun apply(events: Observable<UiEvent>): Observable<UiEvent> {
    val textChanges = events
        .ofType<SearchQueryTextChanged>()
        .map { it.text.trim() }

    val validationErrors = events
        .ofType<SearchClicked>()
        .withLatestFrom(textChanges) { _, text -> text }
        .map(this::validateInput)

    return events.mergeWith(validationErrors)
  }

  private fun validateInput(inputText: String): SearchQueryValidationResult {
    return if (inputText.isBlank()) {
      Invalid(listOf(PatientSearchValidationError.INPUT_EMPTY))
    } else {
      Valid(inputText)
    }
  }
}
