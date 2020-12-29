package org.simple.clinic.instantsearch

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class InstantSearchEffectHandler @Inject constructor(
    private val currentFacility: Lazy<Facility>,
    private val patientRepository: PatientRepository,
    private val schedulers: SchedulersProvider
) {

  fun build(): ObservableTransformer<InstantSearchEffect, InstantSearchEvent> = RxMobius
      .subtypeEffectHandler<InstantSearchEffect, InstantSearchEvent>()
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .addTransformer(LoadAllPatients::class.java, loadAllPatients())
      .addTransformer(SearchWithCriteria::class.java, searchWithCriteria())
      .build()

  private fun searchWithCriteria(): ObservableTransformer<SearchWithCriteria, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { patientRepository.search2(it.criteria, it.facility.uuid) }
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
