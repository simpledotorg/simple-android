package org.simple.clinic.recentpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class AllRecentPatientsUpdate : Update<AllRecentPatientsModel, AllRecentPatientsEvent, AllRecentPatientsEffect> {

  override fun update(
      model: AllRecentPatientsModel,
      event: AllRecentPatientsEvent
  ): Next<AllRecentPatientsModel, AllRecentPatientsEffect> {
    return when (event) {
      is RecentPatientsLoaded -> next(model.recentPatientsLoaded(event.recentPatients))
      is RecentPatientItemClicked -> dispatch(OpenPatientSummary(event.patientUuid))
    }
  }
}
