package org.simple.clinic.drugs.search

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.di.PagingSize
import org.simple.clinic.di.PagingSize.Page.DrugsSearchResults
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.scheduler.SchedulersProvider

class DrugSearchEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val drugsRepository: DrugRepository,
    private val pagerFactory: PagerFactory,
    @PagingSize(DrugsSearchResults) private val drugsSearchResultsPageSize: Int,
    @Assisted private val uiActions: UiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions): DrugSearchEffectHandler
  }

  fun build(): ObservableTransformer<DrugSearchEffect, DrugSearchEvent> {
    return RxMobius
        .subtypeEffectHandler<DrugSearchEffect, DrugSearchEvent>()
        .addTransformer(SearchDrugs::class.java, searchDrugs())
        .addConsumer(SetDrugsSearchResults::class.java, ::setDrugsSearchResults, schedulersProvider.ui())
        .build()
  }

  private fun setDrugsSearchResults(effect: SetDrugsSearchResults) {
    uiActions.setDrugSearchResults(effect.searchResults)
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
