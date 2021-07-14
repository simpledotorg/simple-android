package org.simple.clinic.drugs.search

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.di.PagingSize
import org.simple.clinic.di.PagingSize.Page.DrugsSearchResults
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class DrugSearchEffectHandler @Inject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val drugsRepository: DrugRepository,
    private val pagerFactory: PagerFactory,
    @PagingSize(DrugsSearchResults) private val drugsSearchResultsPageSize: Int
) {

  fun build(): ObservableTransformer<DrugSearchEffect, DrugSearchEvent> {
    return RxMobius
        .subtypeEffectHandler<DrugSearchEffect, DrugSearchEvent>()
        .addTransformer(SearchDrugs::class.java, searchDrugs())
        .build()
  }

  private fun searchDrugs(): ObservableTransformer<SearchDrugs, DrugSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap {
            pagerFactory.createPager(
                sourceFactory = { drugsRepository.search(it.searchQuery) },
                pageSize = drugsSearchResultsPageSize
            )
          }
          .map(::DrugsSearchResultsLoaded)
    }
  }
}
