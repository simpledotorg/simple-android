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
      is PatientSearchResultClicked -> searchResultClicked(model, event)
      NewOngoingPatientEntrySaved -> dispatch(OpenPatientEntryScreen)
      is PatientSearchResultRegisterNewPatient -> {
        val ongoingNewPatientEntry = createOngoingEntryFromSearchCriteria(model.searchCriteria)

        dispatch(SaveNewOngoingPatientEntry(ongoingNewPatientEntry))
      }
    }
  }

  private fun searchResultClicked(
      model: PatientSearchResultsModel,
      event: PatientSearchResultClicked
  ): Next<PatientSearchResultsModel, PatientSearchResultsEffect> {
    val effect = if (!model.hasAdditionalIdentifier)
      OpenPatientSummary(event.patientUuid)
    else
      OpenLinkIdWithPatientScreen(event.patientUuid, model.additionalIdentifier!!)

    return dispatch(effect)
  }

  private fun createOngoingEntryFromSearchCriteria(
      searchCriteria: PatientSearchCriteria
  ): OngoingNewPatientEntry {
    var ongoingNewPatientEntry = when (searchCriteria) {
      is PatientSearchCriteria.Name -> OngoingNewPatientEntry.fromFullName(searchCriteria.patientName)
      is PatientSearchCriteria.PhoneNumber -> OngoingNewPatientEntry.fromPhoneNumber(searchCriteria.phoneNumber)
      is PatientSearchCriteria.NumericCriteria -> OngoingNewPatientEntry.fromPhoneNumber(searchCriteria.numericCriteria)
    }

    if (searchCriteria.additionalIdentifier != null) {
      ongoingNewPatientEntry = ongoingNewPatientEntry.withIdentifier(searchCriteria.additionalIdentifier)
    }

    return ongoingNewPatientEntry
  }
}
