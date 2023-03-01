package org.simple.clinic.monthlyscreeningreports.complete

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class MonthlyScreeningReportCompleteEffectHandler @AssistedInject constructor(
    @Assisted private val viewEffectsConsumer: Consumer<MonthlyScreeningReportCompleteViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<MonthlyScreeningReportCompleteViewEffect>
    ): MonthlyScreeningReportCompleteEffectHandler
  }

  fun build(): ObservableTransformer<MonthlyScreeningReportCompleteEffect, MonthlyScreeningReportCompleteEvent> {
    return RxMobius
        .subtypeEffectHandler<MonthlyScreeningReportCompleteEffect, MonthlyScreeningReportCompleteEvent>()
        .addConsumer(MonthlyScreeningReportCompleteViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }
}
