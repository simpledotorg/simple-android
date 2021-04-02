package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.HideDateErrorMessage
import org.simple.clinic.util.scheduler.SchedulersProvider

class TextInputDatePickerEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: TextInputDatePickerUiActions
) {

  fun build(): ObservableTransformer<TextInputDatePickerEffect, TextInputDatePickerEvent> {
    return RxMobius
        .subtypeEffectHandler<TextInputDatePickerEffect, TextInputDatePickerEvent>()
        .addAction(DismissSheet::class.java, uiActions::dismissSheet, schedulersProvider.ui())
        .addAction(HideDateErrorMessage::class.java, uiActions::hideErrorMessage, schedulersProvider.ui())
        .build()
  }
}
