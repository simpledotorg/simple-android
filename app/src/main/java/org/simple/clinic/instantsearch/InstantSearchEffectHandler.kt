package org.simple.clinic.instantsearch

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.scheduler.SchedulersProvider

class InstantSearchEffectHandler @AssistedInject constructor(
    private val currentFacility: Lazy<Facility>,
    private val patientRepository: PatientRepository,
    private val instantSearchValidator: InstantSearchValidator,
    private val instantSearchConfig: InstantSearchConfig,
    private val pagerFactory: PagerFactory,
    private val schedulers: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<InstantSearchViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<InstantSearchViewEffect>): InstantSearchEffectHandler
  }

  fun build(): ObservableTransformer<InstantSearchEffect, InstantSearchEvent> = RxMobius
      .subtypeEffectHandler<InstantSearchEffect, InstantSearchEvent>()
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .addTransformer(LoadAllPatients::class.java, loadAllPatients())
      .addTransformer(SearchWithCriteria::class.java, searchWithCriteria())
      .addTransformer(ValidateSearchQuery::class.java, validateSearchQuery())
      .addTransformer(SaveNewOngoingPatientEntry::class.java, saveNewOngoingPatientEntry())
      .addTransformer(CheckIfPatientAlreadyHasAnExistingNHID::class.java, checkIfPatientAlreadyHasAnExistingNHID())
      .addConsumer(InstantSearchViewEffect::class.java, viewEffectsConsumer::accept)
      .build()

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
          .switchMap {
            pagerFactory.createPager(
                sourceFactory = { patientRepository.search(it.criteria, it.facility.uuid) },
                pageSize = instantSearchConfig.pagingLoadSize,
                enablePlaceholders = false
            )
          }
          .map(::SearchResultsLoaded)
    }
  }

  private fun loadAllPatients(): ObservableTransformer<LoadAllPatients, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { (facility) ->
            pagerFactory.createPager(
                sourceFactory = { patientRepository.allPatientsInFacility(facilityId = facility.uuid) },
                pageSize = instantSearchConfig.pagingLoadSize,
                enablePlaceholders = false
            )
          }
          .map(::AllPatientsInFacilityLoaded)
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
