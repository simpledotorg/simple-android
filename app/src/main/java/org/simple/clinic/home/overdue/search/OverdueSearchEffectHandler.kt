package org.simple.clinic.home.overdue.search

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class OverdueSearchEffectHandler @Inject constructor(
    private val overdueSearchHistoryPreference: Preference<Set<String>>
) {

  fun build(): ObservableTransformer<OverdueSearchEffect, OverdueSearchEvent> {
    return RxMobius
        .subtypeEffectHandler<OverdueSearchEffect, OverdueSearchEvent>()
        .addTransformer(LoadOverdueSearchHistory::class.java, loadOverdueSearchHistory())
        .build()
  }

  private fun loadOverdueSearchHistory(): ObservableTransformer<LoadOverdueSearchHistory, OverdueSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { overdueSearchHistoryPreference.asObservable() }
          .map(::OverdueSearchHistoryLoaded)
    }
  }
}
