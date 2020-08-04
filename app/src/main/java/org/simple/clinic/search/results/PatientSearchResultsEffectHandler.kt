package org.simple.clinic.search.results

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientSearchResultsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val patientRepository: PatientRepository,
    @Assisted private val uiActions: PatientSearchResultsUiActions
) {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(uiActions: PatientSearchResultsUiActions): PatientSearchResultsEffectHandler
  }

  fun build(): ObservableTransformer<PatientSearchResultsEffect, PatientSearchResultsEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSearchResultsEffect, PatientSearchResultsEvent>()
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummaryScreen(it.patientUuid) }, schedulers.ui())
        .addConsumer(
            OpenLinkIdWithPatientScreen::class.java,
            { uiActions.openLinkIdWithPatientScreen(it.patientUuid, it.additionalIdentifier) },
            schedulers.ui()
        )
        .addTransformer(SaveNewOngoingPatientEntry::class.java, saveNewPatientEntry())
        .build()
  }

  private fun saveNewPatientEntry(): ObservableTransformer<SaveNewOngoingPatientEntry, PatientSearchResultsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { effect ->
            patientRepository
                .saveOngoingEntry(effect.entry)
                .andThen(Observable.just(NewOngoingPatientEntrySaved))
          }
    }
  }
}
