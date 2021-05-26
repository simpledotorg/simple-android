package org.simple.clinic.summary.prescribeddrugs

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.scheduler.SchedulersProvider

class DrugSummaryEffectHandler @AssistedInject constructor(
    private val prescriptionRepository: PrescriptionRepository,
    private val currentFacility: Lazy<Facility>,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: DrugSummaryUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: DrugSummaryUiActions): DrugSummaryEffectHandler
  }

  fun build(): ObservableTransformer<DrugSummaryEffect, DrugSummaryEvent> = RxMobius
      .subtypeEffectHandler<DrugSummaryEffect, DrugSummaryEvent>()
      .addTransformer(LoadPrescribedDrugs::class.java, populatePrescribedDrugs())
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .addConsumer(OpenUpdatePrescribedDrugScreen::class.java, { uiActions.showUpdatePrescribedDrugsScreen(it.patientUuid, it.facility) }, schedulersProvider.ui())
      .build()

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, DrugSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun populatePrescribedDrugs(): ObservableTransformer<LoadPrescribedDrugs, DrugSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { prescriptionRepository.newestPrescriptionsForPatient(patientUuid = it.patientUuid) }
          .map(::PrescribedDrugsLoaded)
    }
  }
}
