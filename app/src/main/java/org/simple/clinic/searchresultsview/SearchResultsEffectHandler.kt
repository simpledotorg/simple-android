package org.simple.clinic.searchresultsview

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class SearchResultsEffectHandler @Inject constructor(
    private val schedulers: SchedulersProvider
) {

  fun build(): ObservableTransformer<SearchResultsEffect, SearchResultsEvent> {
    return RxMobius
        .subtypeEffectHandler<SearchResultsEffect, SearchResultsEvent>()
        .build()
  }
}
