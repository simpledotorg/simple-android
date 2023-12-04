package org.simple.clinic.util

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spotify.mobius.Init
import com.spotify.mobius.Update
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.mobius.MobiusBaseViewModel
import org.simple.clinic.mobius.first

fun <M : Parcelable, E, F, V> Fragment.mobiusViewModels(
    defaultModel: () -> M,
    update: () -> Update<M, E, F>,
    effectHandler: (viewEffectsConsumer: Consumer<V>) -> ObservableTransformer<F, E>,
    init: () -> Init<M, F> = { Init { model -> first(model) } },
) = viewModels<MobiusBaseViewModel<M, E, F, V>> {
  viewModelFactory {
    fun loop(viewEffectsConsumer: Consumer<V>) = RxMobius
        .loop(update(), effectHandler(viewEffectsConsumer))

    initializer {
      val model = defaultModel()
      val modelKey = model.javaClass.name
      val handle = createSavedStateHandle()

      MobiusBaseViewModel(
          modelKey = modelKey,
          savedStateHandle = handle,
          loopFactoryProvider = ::loop,
          defaultModel = defaultModel(),
          init = init(),
      )
    }
  }
}
