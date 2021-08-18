package org.simple.clinic.instantsearch

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.instantsearch.InstantSearchValidator.Result.Empty
import org.simple.clinic.instantsearch.InstantSearchValidator.Result.LengthTooShort
import org.simple.clinic.instantsearch.InstantSearchValidator.Result.Valid
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.NumericCriteria
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.scanid.scannedqrcode.AddToExistingPatient
import org.simple.clinic.scanid.scannedqrcode.RegisterNewPatient
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class InstantSearchUpdate @Inject constructor(
    @Named("date_for_user_input") private val dateTimeFormatter: DateTimeFormatter
) : Update<InstantSearchModel, InstantSearchEvent, InstantSearchEffect> {

  /**
   * Regular expression that matches digits with interleaved white spaces
   **/
  private val digitsRegex = Regex("[\\s*\\d+]+")

  override fun update(
      model: InstantSearchModel,
      event: InstantSearchEvent
  ): Next<InstantSearchModel, InstantSearchEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> currentFacilityLoaded(model, event)
      is AllPatientsInFacilityLoaded -> allPatientsLoaded(model, event)
      is SearchResultsLoaded -> searchResultsLoaded(model, event)
      is SearchQueryValidated -> searchQueryValidated(model, event)
      is SearchResultClicked -> searchResultClicked(model, event)
      is PatientAlreadyHasAnExistingNHID -> dispatch(ShowNHIDErrorDialog)
      is PatientDoesNotHaveAnExistingNHID -> dispatch(OpenLinkIdWithPatientScreen(event.patientId, model.additionalIdentifier!!))
      is SearchQueryChanged -> searchQueryChanged(model, event)
      SavedNewOngoingPatientEntry -> dispatch(OpenPatientEntryScreen(model.facility!!))
      RegisterNewPatientClicked -> registerNewPatient(model)
      is BlankScannedQrCodeResultReceived -> blankScannedQrCodeResult(model, event)
      is OpenQrCodeScannerClicked -> dispatch(OpenQrCodeScanner)
      is SearchResultsLoadStateChanged -> searchResultsLoadStateChanged(model, event)
      InstantSearchScreenShown -> instantSearchScreenShown(model)
    }
  }

  private fun searchQueryChanged(
      model: InstantSearchModel,
      event: SearchQueryChanged
  ): Next<InstantSearchModel, InstantSearchEffect> {
    return if (event.searchQuery != model.searchQuery) {
      next(model.searchQueryChanged(event.searchQuery), ValidateSearchQuery(event.searchQuery))
    } else {
      noChange()
    }
  }

  private fun instantSearchScreenShown(model: InstantSearchModel): Next<InstantSearchModel, InstantSearchEffect> {
    val effects = mutableSetOf<InstantSearchEffect>()

    if (model.hasFacility && model.hasSearchQuery) {
      effects.add(PrefillSearchQuery(model.searchQuery!!))
      effects.add(ValidateSearchQuery(model.searchQuery))
    }

    // When a scanned BP Passport does not result in a match, we bring up a bottom sheet which asks
    // whether this is a new registration or an existing patient. If we show the keyboard in these
    // cases, the UI is janky since the keyboard pops up and immediately another bottom sheet pops up.
    // This improves the experience by showing the keyboard only if we have arrived here by searching
    // for a patient by the name
    if (!model.hasAdditionalIdentifier) effects.add(ShowKeyboard)

    return dispatch(effects)
  }

  private fun searchResultsLoadStateChanged(
      model: InstantSearchModel,
      event: SearchResultsLoadStateChanged
  ): Next<InstantSearchModel, InstantSearchEffect> {
    return next(model.loadStateChanged(event.instantSearchProgressState))
  }

  private fun currentFacilityLoaded(
      model: InstantSearchModel,
      event: CurrentFacilityLoaded
  ): Next<InstantSearchModel, InstantSearchEffect> {
    val facilityLoadedModel = model.facilityLoaded(event.facility)
    val effect = if (model.hasSearchQuery) {
      PrefillSearchQuery(model.searchQuery!!)
    } else {
      LoadAllPatients(event.facility)
    }

    return next(facilityLoadedModel, effect)
  }

  private fun searchResultClicked(
      model: InstantSearchModel,
      event: SearchResultClicked
  ): Next<InstantSearchModel, InstantSearchEffect> =
      if (model.isAdditionalIdentifierAnNHID) {
        dispatch(CheckIfPatientAlreadyHasAnExistingNHID(event.patientId))
      } else {
        searchResultClickedWithoutNHID(model, event.patientId)
      }

  private fun blankScannedQrCodeResult(
      model: InstantSearchModel,
      event: BlankScannedQrCodeResultReceived
  ): Next<InstantSearchModel, InstantSearchEffect> {
    return when (event.blankScannedQRCodeResult) {
      AddToExistingPatient -> dispatch(ShowKeyboard)
      RegisterNewPatient -> registerNewPatient(model)
    }
  }

  private fun registerNewPatient(model: InstantSearchModel): Next<InstantSearchModel, InstantSearchEffect> {
    var ongoingPatientEntry = when (val searchCriteria = searchCriteriaFromInput(model.searchQuery.orEmpty(), model.additionalIdentifier)) {
      is Name -> OngoingNewPatientEntry.fromFullName(searchCriteria.patientName)
      is NumericCriteria -> OngoingNewPatientEntry.default()
    }

    if (model.canBePrefilled) {
      ongoingPatientEntry = ongoingPatientEntry.withPatientPrefillInfo(model.patientPrefillInfo!!, model.additionalIdentifier!!, dateTimeFormatter)
    }

    if (model.isAdditionalIdentifierBpPassport) {
      ongoingPatientEntry = ongoingPatientEntry.withIdentifier(model.additionalIdentifier!!)
    }

    return dispatch(SaveNewOngoingPatientEntry(ongoingPatientEntry))
  }

  private fun searchResultClickedWithoutNHID(
      model: InstantSearchModel,
      patientId: UUID
  ): Next<InstantSearchModel, InstantSearchEffect> {
    val effect = if (model.hasAdditionalIdentifier)
      OpenLinkIdWithPatientScreen(patientId, model.additionalIdentifier!!)
    else
      OpenPatientSummary(patientId)

    return dispatch(effect)
  }

  private fun searchQueryValidated(
      model: InstantSearchModel,
      event: SearchQueryValidated
  ): Next<InstantSearchModel, InstantSearchEffect> {
    return when (val validationResult = event.result) {
      is Valid -> {
        val criteria = searchCriteriaFromInput(validationResult.searchQuery, model.additionalIdentifier)
        dispatch(
            SearchWithCriteria(criteria, model.facility!!)
        )
      }
      LengthTooShort -> noChange()
      Empty -> dispatch(
          LoadAllPatients(model.facility!!)
      )
    }
  }

  private fun searchCriteriaFromInput(
      inputString: String,
      additionalIdentifier: Identifier?
  ): PatientSearchCriteria {
    return when {
      digitsRegex.matches(inputString) -> NumericCriteria(inputString.filterNot { it.isWhitespace() }, additionalIdentifier)
      else -> Name(inputString, additionalIdentifier)
    }
  }

  private fun searchResultsLoaded(
      model: InstantSearchModel,
      event: SearchResultsLoaded
  ): Next<InstantSearchModel, InstantSearchEffect> {
    if (!model.hasSearchQuery) return noChange()

    return dispatch(ShowPatientSearchResults(event.patientsSearchResults, model.facility!!, model.searchQuery!!))
  }

  private fun allPatientsLoaded(
      model: InstantSearchModel,
      event: AllPatientsInFacilityLoaded
  ): Next<InstantSearchModel, InstantSearchEffect> {
    if (model.hasSearchQuery) return noChange()

    return dispatch(ShowAllPatients(event.patients, model.facility!!))
  }
}
