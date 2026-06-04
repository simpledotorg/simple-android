package org.simple.clinic.patientattribute.entry

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class BMIEntryEffectHandler @AssistedInject constructor(
    @Assisted private val ui: BMIEntryUi,
    private val schedulersProvider: SchedulersProvider,
) {

  @AssistedFactory
  interface Factory {
    fun create(ui: BMIEntryUi): BMIEntryEffectHandler
  }

  fun build(): ObservableTransformer<BMIEntryEffect, BMIEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<BMIEntryEffect, BMIEntryEvent>()
        .addAction(CloseSheet::class.java, ui::closeSheet, schedulersProvider.ui())
        .addAction(ChangeFocusToHeight::class.java, ui::changeFocusToHeight, schedulersProvider.ui())
        .addAction(ChangeFocusToWeight::class.java, ui::changeFocusToWeight, schedulersProvider.ui())
        .build()
  }
}
