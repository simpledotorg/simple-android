package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import javax.inject.Inject

typealias AllPatientsInFacilityUiChange = (AllPatientsInFacilityUi) -> Unit

class AllPatientsInFacilityUiChangeProducer @Inject constructor() : ObservableTransformer<AllPatientsInFacilityUiState, AllPatientsInFacilityUiChange> {
  override fun apply(uiStates: Observable<AllPatientsInFacilityUiState>): ObservableSource<AllPatientsInFacilityUiChange> {
    val queriedPatientsStates = uiStates.filter { it.patientsQueried }

    return Observable.merge(
        noPatientsInFacilityUiChanges(queriedPatientsStates),
        hasPatientsInFacilityUiChanges(queriedPatientsStates)
    )
  }

  private fun hasPatientsInFacilityUiChanges(
      queriedPatientsStates: Observable<AllPatientsInFacilityUiState>
  ): Observable<AllPatientsInFacilityUiChange> {
    return queriedPatientsStates
        .filter { it.patients.isNotEmpty() }
        .map { { ui: AllPatientsInFacilityUi -> ui.showPatients(it.facility!!, it.patients) } }
  }

  private fun noPatientsInFacilityUiChanges(
      queriedPatientsStates: Observable<AllPatientsInFacilityUiState>
  ): Observable<AllPatientsInFacilityUiChange> {
    return queriedPatientsStates
        .filter { it.patients.isEmpty() }
        .map { { view: AllPatientsInFacilityUi -> view.showNoPatientsFound(it.facility!!.name) } }
  }
}
