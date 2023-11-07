package org.simple.clinic.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spotify.mobius.Init
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusLoopViewModel
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

fun <M : Any, E, F, V> Fragment.mobiusViewModels(
    defaultModel: () -> M,
    init: () -> Init<M, F>,
    update: () -> Update<M, E, F>,
    effectHandler: (viewEffectsConsumer: Consumer<V>) -> ObservableTransformer<F, E>
) = viewModels<MobiusLoopViewModel<M, E, F, V>> {
  object : ViewModelProvider.Factory {
    private fun loop(viewEffectsConsumer: Consumer<V>) = RxMobius
        .loop(update(), effectHandler(viewEffectsConsumer))

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return MobiusLoopViewModel.create(
          ::loop,
          defaultModel(),
          init()
      ) as T
    }
  }
}
