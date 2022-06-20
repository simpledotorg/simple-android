package org.simple.clinic.home.overdue.search

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class OverdueSearchEffectHandler @Inject constructor(
    private val overdueSearchHistory: OverdueSearchHistory,
    private val overdueSearchQueryValidator: OverdueSearchQueryValidator,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<OverdueSearchEffect, OverdueSearchEvent> {
    return RxMobius
        .subtypeEffectHandler<OverdueSearchEffect, OverdueSearchEvent>()
        .addTransformer(LoadOverdueSearchHistory::class.java, loadOverdueSearchHistory())
        .addTransformer(ValidateOverdueSearchQuery::class.java, validateOverdueSearchQuery())
        .addConsumer(AddQueryToOverdueSearchHistory::class.java, ::addQueryToSearchHistory)
        .build()
  }

  private fun addQueryToSearchHistory(effect: AddQueryToOverdueSearchHistory) {
    overdueSearchHistory.add(effect.searchQuery)
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
          .switchMap { overdueSearchHistory.fetch() }
          .map(::OverdueSearchHistoryLoaded)
    }
  }
}
