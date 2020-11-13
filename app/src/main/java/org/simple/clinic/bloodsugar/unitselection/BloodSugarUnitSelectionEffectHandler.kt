package org.simple.clinic.bloodsugar.unitselection

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodSugarUnitSelectionEffectHandler constructor(
    private val schedulersProvider: SchedulersProvider,
    private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>
) {

  fun build(): ObservableTransformer<BloodSugarUnitSelectionEffect, BloodSugarUnitSelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarUnitSelectionEffect, BloodSugarUnitSelectionEvent>()
        .addTransformer(SaveBloodSugarUnitSelection::class.java, updateBloodSugarUnitPreferenceSelection())
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
