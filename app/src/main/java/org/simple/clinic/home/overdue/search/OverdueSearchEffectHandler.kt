package org.simple.clinic.home.overdue.search

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.OverdueSearchHistory
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class OverdueSearchEffectHandler @Inject constructor(
    @TypedPreference(OverdueSearchHistory) private val overdueSearchHistoryPreference: Preference<String>,
    private val overdueSearchQueryValidator: OverdueSearchQueryValidator,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<OverdueSearchEffect, OverdueSearchEvent> {
    return RxMobius
        .subtypeEffectHandler<OverdueSearchEffect, OverdueSearchEvent>()
        .addTransformer(LoadOverdueSearchHistory::class.java, loadOverdueSearchHistory())
        .addTransformer(ValidateOverdueSearchQuery::class.java, validateOverdueSearchQuery())
        .build()
  }

  private fun validateOverdueSearchQuery(): ObservableTransformer<ValidateOverdueSearchQuery, OverdueSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { overdueSearchQueryValidator.validate(it.searchQuery) }
          .map(::OverdueSearchQueryValidated)
    }
  }

  private fun loadOverdueSearchHistory(): ObservableTransformer<LoadOverdueSearchHistory, OverdueSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { overdueSearchHistoryPreference.asObservable() }
          .map {
            // TODO (SM): Extract overdue search history handling to a separate class
            it.split(", ").toSet()
          }
          .map(::OverdueSearchHistoryLoaded)
    }
  }
}
