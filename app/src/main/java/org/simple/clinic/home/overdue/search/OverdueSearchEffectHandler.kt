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
import org.simple.clinic.overdue.download.OverdueDownloadScheduler
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
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
    private val overdueDownloadScheduler: OverdueDownloadScheduler,
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
        .addTransformer(ReplaceSelectedAppointmentIds::class.java, replaceSelectedAppointmentIds())
        .addConsumer(ScheduleDownload::class.java, ::scheduleDownload)
        .addConsumer(SelectAllAppointmentIds::class.java, ::selectAllAppointmentIds)
        .addTransformer(LoadSearchResultsAppointmentIds::class.java, loadSearchResultsAppointmentIds())
        .build()
  }

  private fun loadSearchResultsAppointmentIds(): ObservableTransformer<LoadSearchResultsAppointmentIds, OverdueSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect ->
            val appointmentIds = appointmentRepository.searchOverduePatientsImmediate(
                searchQuery = effect.searchQuery,
                since = effect.since,
                facilityId = currentFacility.get().uuid
            ).map { it.appointment.uuid }.toSet()

            SearchResultsAppointmentIdsLoaded(
                buttonType = effect.buttonType,
                searchResultsAppointmentIds = appointmentIds
            )
          }
    }
  }

  private fun selectAllAppointmentIds(effect: SelectAllAppointmentIds) {
    overdueAppointmentSelector.addSelectedIds(effect.appointmentIds)
  }

  private fun scheduleDownload(effect: ScheduleDownload) {
    overdueDownloadScheduler.schedule(CSV)
  }

  private fun replaceSelectedAppointmentIds(): ObservableTransformer<ReplaceSelectedAppointmentIds, OverdueSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.computation())
          .map { (appointmentIds, buttonType) ->
            overdueAppointmentSelector.replaceSelectedIds(appointmentIds)
            return@map buttonType
          }
          .map { SelectedAppointmentIdsReplaced(it) }
    }
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
