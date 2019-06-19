package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

typealias AllPatientsInFacilityUiChange = (AllPatientsInFacilityView) -> Unit

class AllPatientsInFacilityUiChangeProducer : ObservableTransformer<AllPatientsInFacilityViewState, AllPatientsInFacilityUiChange> {
  override fun apply(viewStates: Observable<AllPatientsInFacilityViewState>): ObservableSource<AllPatientsInFacilityUiChange> {
    val queriedPatientsViewStates = viewStates.filter { it.patientsQueried }

    val noPatientsInFacilityUiChanges = queriedPatientsViewStates
        .filter { it.patients.isEmpty() }
        .map { { view: AllPatientsInFacilityView -> view.showNoPatientsFound(it.facility!!.name) } }

    val hasPatientsInFacilityUiChanges = queriedPatientsViewStates
        .filter { it.patients.isNotEmpty() }
        .map { { view: AllPatientsInFacilityView -> view.showPatients(it.facility!!, it.patients) } }

    return Observable.merge(noPatientsInFacilityUiChanges, hasPatientsInFacilityUiChanges)
  }
}
