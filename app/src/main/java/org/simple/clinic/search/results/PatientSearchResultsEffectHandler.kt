package org.simple.clinic.search.results

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientSearchResultsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: PatientSearchResultsUiActions
) {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(uiActions: PatientSearchResultsUiActions): PatientSearchResultsEffectHandler
  }

  fun build(): ObservableTransformer<PatientSearchResultsEffect, PatientSearchResultsEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSearchResultsEffect, PatientSearchResultsEvent>()
        .build()
  }
}
