package org.simple.clinic.recentpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class AllRecentPatientsUpdate : Update<AllRecentPatientsModel, AllRecentPatientsEvent, AllRecentPatientsEffect> {

  override fun update(
      model: AllRecentPatientsModel,
      event: AllRecentPatientsEvent
  ): Next<AllRecentPatientsModel, AllRecentPatientsEffect> {
    return when (event) {
      is RecentPatientsLoaded -> dispatch(ShowRecentPatients(event.recentPatients))
      is RecentPatientItemClicked -> dispatch(OpenPatientSummary(event.patientUuid))
    }
  }
}
