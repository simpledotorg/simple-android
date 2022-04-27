package org.simple.clinic.drugstockreminders.enterdrugstock

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DrugStockFormURL
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.Optional
import javax.inject.Inject

class EnterDrugStockEffectHandler @Inject constructor(
    private val schedulersProvider: SchedulersProvider,
    @TypedPreference(DrugStockFormURL) private val drugStockFormUrlPreference: Preference<Optional<String>>
) {

  fun build(): ObservableTransformer<EnterDrugStockEffect, EnterDrugStockEvent> {
    return RxMobius
        .subtypeEffectHandler<EnterDrugStockEffect, EnterDrugStockEvent>()
        .addTransformer(LoadDrugStockFormUrl::class.java, loadDrugStockFormUrl())
        .build()
  }

  private fun loadDrugStockFormUrl(): ObservableTransformer<LoadDrugStockFormUrl, EnterDrugStockEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { drugStockFormUrlPreference.get() }
          .map(::DrugStockFormUrlLoaded)
    }
  }
}
