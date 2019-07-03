package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.plumbing.BaseUiChangeProducer
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

typealias AllPatientsInFacilityUiChange = (AllPatientsInFacilityUi) -> Unit

class AllPatientsInFacilityUiChangeProducer @Inject constructor(
    schedulersProvider: SchedulersProvider
) : BaseUiChangeProducer<AllPatientsInFacilityUiState, AllPatientsInFacilityUi>(schedulersProvider.ui()) {

  override fun uiChanges(): ObservableTransformer<AllPatientsInFacilityUiState, AllPatientsInFacilityUiChange> {
    return ObservableTransformer { uiStates ->
      val queriedPatientsStates = uiStates.filter { it.patientsQueried }

      Observable.merge(
          noPatientsInFacilityUiChanges(queriedPatientsStates),
          hasPatientsInFacilityUiChanges(queriedPatientsStates)
      )
    }
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
