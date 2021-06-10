package org.simple.clinic.instantsearch

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class InstantSearchEffectHandler @AssistedInject constructor(
    private val currentFacility: Lazy<Facility>,
    private val patientRepository: PatientRepository,
    private val instantSearchValidator: InstantSearchValidator,
    private val instantSearchConfig: InstantSearchConfig,
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: InstantSearchUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: InstantSearchUiActions): InstantSearchEffectHandler
  }

  fun build(): ObservableTransformer<InstantSearchEffect, InstantSearchEvent> = RxMobius
      .subtypeEffectHandler<InstantSearchEffect, InstantSearchEvent>()
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .addTransformer(LoadAllPatients::class.java, loadAllPatients())
      .addTransformer(SearchWithCriteria::class.java, searchWithCriteria())
      .addConsumer(ShowAllPatients::class.java, { uiActions.showAllPatients(it.patients, it.facility) }, schedulers.ui())
      .addConsumer(ShowPatientSearchResults::class.java, { uiActions.showPatientsSearchResults(it.patients, it.facility, it.searchQuery) }, schedulers.ui())
      .addTransformer(ValidateSearchQuery::class.java, validateSearchQuery())
      .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientId) }, schedulers.ui())
      .addConsumer(OpenLinkIdWithPatientScreen::class.java, { uiActions.openLinkIdWithPatientScreen(it.patientId, it.identifier) }, schedulers.ui())
      .addConsumer(OpenScannedQrCodeSheet::class.java, { uiActions.openScannedQrCodeSheet(it.identifier) }, schedulers.ui())
      .addConsumer(ShowNoPatientsInFacility::class.java, { uiActions.showNoPatientsInFacility(it.facility) }, schedulers.ui())
      .addAction(ShowNoSearchResults::class.java, { uiActions.showNoSearchResults() }, schedulers.ui())
      .addAction(HideNoPatientsInFacility::class.java, uiActions::hideNoPatientsInFacility, schedulers.ui())
      .addAction(HideNoSearchResults::class.java, uiActions::hideNoSearchResults, schedulers.ui())
      .addTransformer(SaveNewOngoingPatientEntry::class.java, saveNewOngoingPatientEntry())
      .addConsumer(OpenPatientEntryScreen::class.java, { uiActions.openPatientEntryScreen(it.facility) }, schedulers.ui())
      .addAction(ShowKeyboard::class.java, { uiActions.showKeyboard() }, schedulers.ui())
      .addAction(OpenQrCodeScanner::class.java, uiActions::openQrCodeScanner, schedulers.ui())
      .addTransformer(CheckIfPatientAlreadyHasAnExistingNHID::class.java, checkIfPatientAlreadyHasAnExistingNHID())
      .addAction(ShowNHIDErrorDialog::class.java, uiActions::showNHIDErrorDialog, schedulers.ui())
      .addConsumer(PrefillSearchQuery::class.java, ::prefillSearchQuery, schedulers.ui())
      .build()

  private fun prefillSearchQuery(effect: PrefillSearchQuery) {
    uiActions.prefillSearchQuery(effect.searchQuery)
  }

  private fun saveNewOngoingPatientEntry(): ObservableTransformer<SaveNewOngoingPatientEntry, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { patientRepository.saveOngoingEntry(it.ongoingNewPatientEntry) }
          .map { SavedNewOngoingPatientEntry }
    }
  }

  private fun validateSearchQuery(): ObservableTransformer<ValidateSearchQuery, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.computation())
          .map {
            instantSearchValidator.validate(
                searchQuery = it.searchQuery,
                minLengthForSearchQuery = instantSearchConfig.minLengthOfSearchQuery
            )
          }
          .map(::SearchQueryValidated)
    }
  }

  private fun checkIfPatientAlreadyHasAnExistingNHID(): ObservableTransformer<CheckIfPatientAlreadyHasAnExistingNHID, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { patientId -> patientRepository.patientProfileImmediate(patientId.patientId) }
          .map { patientProfile -> patientProfile.get().withoutDeletedBusinessIds() }
          .map { patientProfile -> checkIfPatientProfileHasNationalHealthId(patientProfile) }
    }
  }

  private fun checkIfPatientProfileHasNationalHealthId(patientProfile: PatientProfile) =
      if (patientProfile.hasNationalHealthID) {
        PatientAlreadyHasAnExistingNHID
      } else {
        PatientDoesNotHaveAnExistingNHID(patientProfile.patientUuid)
      }

  private fun searchWithCriteria(): ObservableTransformer<SearchWithCriteria, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { patientRepository.search(it.criteria, it.facility.uuid) }
          .map(::SearchResultsLoaded)
    }
  }

  private fun loadAllPatients(): ObservableTransformer<LoadAllPatients, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { patientRepository.allPatientsInFacility(it.facility) }
          .map(::AllPatientsLoaded)
    }
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }
}
