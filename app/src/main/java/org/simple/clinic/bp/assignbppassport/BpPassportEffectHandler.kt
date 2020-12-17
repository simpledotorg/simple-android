package org.simple.clinic.bp.assignbppassport

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class BpPassportEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val currentFacility: Lazy<Facility>,
    @Assisted private val uiActions: BpPassportUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: BpPassportUiActions): BpPassportEffectHandler
  }

  fun build(): ObservableTransformer<BpPassportEffect, BpPassportEvent> {
    return RxMobius
        .subtypeEffectHandler<BpPassportEffect, BpPassportEvent>()
        .addTransformer(SaveNewOngoingPatientEntry::class.java, saveNewPatientEntry())
        .addTransformer(FetchCurrentFacility::class.java, fetchCurrentFacility())
        .addConsumer(OpenPatientEntryScreen::class.java, { uiActions.openPatientEntryScreen(it.facility) }, schedulersProvider.ui())
        .addAction(CloseSheet::class.java, { uiActions.closeSheet() }, schedulersProvider.ui())
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
