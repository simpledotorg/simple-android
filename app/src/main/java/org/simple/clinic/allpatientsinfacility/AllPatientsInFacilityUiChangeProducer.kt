package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

typealias AllPatientsInFacilityUiChange = (AllPatientsInFacilityUi) -> Unit

class AllPatientsInFacilityUiChangeProducer : ObservableTransformer<AllPatientsInFacilityUiState, AllPatientsInFacilityUiChange> {
  override fun apply(uiStates: Observable<AllPatientsInFacilityUiState>): ObservableSource<AllPatientsInFacilityUiChange> {
    val queriedPatientsViewStates = uiStates.filter { it.patientsQueried }

    val noPatientsInFacilityUiChanges = queriedPatientsViewStates
        .filter { it.patients.isEmpty() }
        .map { { view: AllPatientsInFacilityUi -> view.showNoPatientsFound(it.facility!!.name) } }

    val hasPatientsInFacilityUiChanges = queriedPatientsViewStates
        .filter { it.patients.isNotEmpty() }
        .map { { ui: AllPatientsInFacilityUi -> ui.showPatients(it.facility!!, it.patients) } }

    return Observable.merge(noPatientsInFacilityUiChanges, hasPatientsInFacilityUiChanges)
  }
}
