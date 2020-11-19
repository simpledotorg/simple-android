package org.simple.clinic.bloodsugar.unitselection

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodSugarUnitSelectionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>,
    @Assisted private val uiActions: BloodSugarUnitSelectionUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: BloodSugarUnitSelectionUiActions): BloodSugarUnitSelectionEffectHandler
  }

  fun build(): ObservableTransformer<BloodSugarUnitSelectionEffect, BloodSugarUnitSelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarUnitSelectionEffect, BloodSugarUnitSelectionEvent>()
        .addTransformer(SaveBloodSugarUnitSelection::class.java, updateBloodSugarUnitPreferenceSelection())
        .addAction(CloseDialog::class.java, uiActions::closeDialog, schedulersProvider.ui())
        .addConsumer(PreFillBloodSugarUnitSelected::class.java, { uiActions.prefillBloodSugarUnitSelection(it.bloodSugarUnitPreference) }, schedulersProvider.ui())
        .build()
  }

  private fun updateBloodSugarUnitPreferenceSelection(): ObservableTransformer<SaveBloodSugarUnitSelection, BloodSugarUnitSelectionEvent> {
    return ObservableTransformer { effect ->
      effect
          .observeOn(schedulersProvider.io())
          .doOnNext { bloodSugarUnitPreference.set(it.bloodSugarUnitSelection) }
          .map { BloodSugarUnitSelectionUpdated }
    }
  }
}
