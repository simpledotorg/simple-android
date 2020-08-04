package org.simple.clinic.search.results

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientSearchCriteria

class PatientSearchResultsUpdate : Update<PatientSearchResultsModel, PatientSearchResultsEvent, PatientSearchResultsEffect> {

  override fun update(
      model: PatientSearchResultsModel,
      event: PatientSearchResultsEvent
  ): Next<PatientSearchResultsModel, PatientSearchResultsEffect> {
    return when (event) {
      is PatientSearchResultClicked -> {
        val effect = if (!model.hasAdditionalIdentifier)
          OpenPatientSummary(event.patientUuid)
        else
          OpenLinkIdWithPatientScreen(event.patientUuid, model.additionalIdentifier!!)

        dispatch(effect)
      }
      NewOngoingPatientEntrySaved -> dispatch(OpenPatientEntryScreen)
      is PatientSearchResultRegisterNewPatient -> {
        val ongoingNewPatientEntry = createOngoingEntryFromSearchCriteria(model.searchCriteria)

        dispatch(SaveNewOngoingPatientEntry(ongoingNewPatientEntry))
      }
    }
  }

  private fun createOngoingEntryFromSearchCriteria(
      searchCriteria: PatientSearchCriteria
  ): OngoingNewPatientEntry {
    var ongoingNewPatientEntry = when (searchCriteria) {
      is PatientSearchCriteria.Name -> OngoingNewPatientEntry.fromFullName(searchCriteria.patientName)
      is PatientSearchCriteria.PhoneNumber -> OngoingNewPatientEntry.fromPhoneNumber(searchCriteria.phoneNumber)
    }

    if (searchCriteria.additionalIdentifier != null) {
      ongoingNewPatientEntry = ongoingNewPatientEntry.withIdentifier(searchCriteria.additionalIdentifier)
    }

    return ongoingNewPatientEntry
  }
}
