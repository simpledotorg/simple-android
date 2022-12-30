package org.simple.clinic.bloodsugar.unitselection

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodSugarUnitSelectionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>,
    @Assisted private val uiActions: BloodSugarUnitSelectionUiActions,
    @Assisted val viewEffectsConsumer: Consumer<BloodSugarUnitSelectionViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: BloodSugarUnitSelectionUiActions,
        viewEffectsConsumer: Consumer<BloodSugarUnitSelectionViewEffect>
    ): BloodSugarUnitSelectionEffectHandler
  }

  fun build(): ObservableTransformer<BloodSugarUnitSelectionEffect, BloodSugarUnitSelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarUnitSelectionEffect, BloodSugarUnitSelectionEvent>()
        .addTransformer(SaveBloodSugarUnitSelection::class.java, updateBloodSugarUnitPreferenceSelection())
        .addConsumer(PreFillBloodSugarUnitSelected::class.java, { uiActions.prefillBloodSugarUnitSelection(it.bloodSugarUnitPreference) }, schedulersProvider.ui())
        .addConsumer(BloodSugarUnitSelectionViewEffect::class.java, viewEffectsConsumer::accept)
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
