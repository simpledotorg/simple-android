package org.simple.clinic.recentpatient

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.di.PagingSize
import org.simple.clinic.di.PagingSize.Page.AllRecentPatients
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.scheduler.SchedulersProvider

class AllRecentPatientsEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val currentFacility: Lazy<Facility>,
    private val pagerFactory: PagerFactory,
    @PagingSize(AllRecentPatients) private val allRecentPatientsPagingSize: Int,
    @Assisted private val uiActions: AllRecentPatientsUiActions,
    @Assisted private val viewEffectsConsumer: Consumer<AllRecentPatientsViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: AllRecentPatientsUiActions,
        viewEffectsConsumer: Consumer<AllRecentPatientsViewEffect>
    ): AllRecentPatientsEffectHandler
  }

  fun build(): ObservableTransformer<AllRecentPatientsEffect, AllRecentPatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<AllRecentPatientsEffect, AllRecentPatientsEvent>()
        .addTransformer(LoadAllRecentPatients::class.java, loadAllRecentPatients())
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientUuid) }, schedulersProvider.ui())
        .addConsumer(ShowRecentPatients::class.java, ::showRecentPatients, schedulersProvider.ui())
        .build()
  }

  private fun showRecentPatients(effect: ShowRecentPatients) {
    uiActions.showRecentPatients(effect.recentPatients)
  }

  private fun loadAllRecentPatients(): ObservableTransformer<LoadAllRecentPatients, AllRecentPatientsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap {
            val facilityId = currentFacility.get().uuid

            pagerFactory.createPager(
                sourceFactory = { patientRepository.recentPatients(facilityUuid = facilityId) },
                pageSize = allRecentPatientsPagingSize,
                enablePlaceholders = false
            )
          }
          .map(::RecentPatientsLoaded)
    }
  }
}
