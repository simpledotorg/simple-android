package org.simple.clinic.teleconsultlog.medicinefrequency

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class MedicineFrequencyEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: MedicineFrequencySheetUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: MedicineFrequencySheetUiActions): MedicineFrequencyEffectHandler
  }

  fun build(): ObservableTransformer<MedicineFrequencyEffect, MedicineFrequencyEvent> {
    return RxMobius
        .subtypeEffectHandler<MedicineFrequencyEffect, MedicineFrequencyEvent>()
        .addConsumer(SetMedicineFrequency::class.java, { uiActions.setMedicineFrequency(it.medicineFrequency) }, schedulersProvider.ui())
        .addConsumer(SaveMedicineFrequency::class.java, { uiActions.saveMedicineFrequency(it.medicineFrequency) }, schedulersProvider.ui())
        .build()
  }
}
