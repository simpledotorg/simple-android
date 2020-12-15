package org.simple.clinic.bp.assignbppassport

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class BpPassportEffectHandler constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val currentFacility: Lazy<Facility>
) {

  fun build(): ObservableTransformer<BpPassportEffect, BpPassportEvent> {
    return RxMobius
        .subtypeEffectHandler<BpPassportEffect, BpPassportEvent>()
        .addTransformer(SaveNewOngoingPatientEntry::class.java, saveNewPatientEntry())
        .addTransformer(FetchCurrentFacility::class.java, fetchCurrentFacility())
        .build()
  }

  private fun fetchCurrentFacility(): ObservableTransformer<FetchCurrentFacility, BpPassportEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { currentFacility.get() }
          .map { CurrentFacilityRetrieved(it) }
    }
  }

  private fun saveNewPatientEntry(): ObservableTransformer<SaveNewOngoingPatientEntry, BpPassportEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { effect -> patientRepository.saveOngoingEntry(effect.entry) }
          .map { NewOngoingPatientEntrySaved }
    }
  }
}
