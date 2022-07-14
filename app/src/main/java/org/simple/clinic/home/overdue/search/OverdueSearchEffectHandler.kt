package org.simple.clinic.home.overdue.search

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import kotlinx.coroutines.CoroutineScope
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.OverdueAppointmentSelector
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.scheduler.SchedulersProvider

class OverdueSearchEffectHandler @AssistedInject constructor(
    private val overdueSearchHistory: OverdueSearchHistory,
    private val overdueSearchQueryValidator: OverdueSearchQueryValidator,
    private val schedulersProvider: SchedulersProvider,
    private val appointmentRepository: AppointmentRepository,
    private val pagerFactory: PagerFactory,
    private val overdueSearchConfig: OverdueSearchConfig,
    private val currentFacility: Lazy<Facility>,
    private val overdueAppointmentSelector: OverdueAppointmentSelector,
    @Assisted private val viewEffectsConsumer: Consumer<OverdueSearchViewEffect>,
    @Assisted private val pagingCacheScope: CoroutineScope
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<OverdueSearchViewEffect>,
        pagingCacheScope: CoroutineScope
    ): OverdueSearchEffectHandler
  }

  fun build(): ObservableTransformer<OverdueSearchEffect, OverdueSearchEvent> {
    return RxMobius
        .subtypeEffectHandler<OverdueSearchEffect, OverdueSearchEvent>()
        .addTransformer(LoadOverdueSearchHistory::class.java, loadOverdueSearchHistory())
        .addTransformer(ValidateOverdueSearchQuery::class.java, validateOverdueSearchQuery())
        .addConsumer(AddQueryToOverdueSearchHistory::class.java, ::addQueryToSearchHistory)
        .addTransformer(SearchOverduePatients::class.java, searchOverduePatients())
        .addConsumer(OverdueSearchViewEffect::class.java, viewEffectsConsumer::accept)
        .addConsumer(ToggleOverdueAppointmentSelection::class.java, ::toggleOverdueAppointmentSelection, schedulersProvider.computation())
        .addTransformer(LoadSelectedOverdueAppointmentIds::class.java, loadSelectedOverdueAppointmentIds())
        .addConsumer(ClearSelectedOverdueAppointments::class.java, ::clearSelectedOverdueAppointments)
        .build()
  }

  private fun clearSelectedOverdueAppointments(effect: ClearSelectedOverdueAppointments) {
    overdueAppointmentSelector.clearSelection()
  }

  private fun loadSelectedOverdueAppointmentIds(): ObservableTransformer<LoadSelectedOverdueAppointmentIds, OverdueSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.computation())
          .switchMap { overdueAppointmentSelector.selectedAppointmentIdsStream }
          .map(::SelectedOverdueAppointmentsLoaded)
    }
  }

  private fun toggleOverdueAppointmentSelection(effect: ToggleOverdueAppointmentSelection) {
    overdueAppointmentSelector.toggleSelection(effect.appointmentId)
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

  private fun searchOverduePatients(): ObservableTransformer<SearchOverduePatients, OverdueSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            currentFacility.get().uuid to it
          }
          .switchMap { (facilityId, searchOverduePatientsEffect) ->
            pagerFactory.createPager(
                sourceFactory = {
                  appointmentRepository.searchOverduePatient(
                      searchOverduePatientsEffect.searchQuery,
                      searchOverduePatientsEffect.since,
                      facilityId
                  )
                },
                pageSize = overdueSearchConfig.pagingLoadSize,
                enablePlaceholders = false,
                cacheScope = pagingCacheScope
            )
          }
          .map(::OverdueSearchResultsLoaded)
    }
  }
}
