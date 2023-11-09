package org.simple.clinic.mobius

import android.os.Parcelable
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.distinctUntilChanged
import com.spotify.mobius.Init
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.MobiusLoopViewModel
import com.spotify.mobius.android.runners.MainThreadWorkRunner
import com.spotify.mobius.functions.Consumer
import timber.log.Timber

private const val MAX_EFFECTS_QUEUE_SIZE = 100

class MobiusBaseViewModel<M : Parcelable, E, F, V>(
    private val modelKey: String,
    private val savedStateHandle: SavedStateHandle,
    loopFactoryProvider: (Consumer<V>) -> MobiusLoop.Builder<M, E, F>,
    defaultModel: M,
    init: Init<M, F>,
) : MobiusLoopViewModel<M, E, F, V>(
    loopFactoryProvider,
    savedStateHandle.get<M>(modelKey) ?: defaultModel,
    init,
    MainThreadWorkRunner.create(),
    MAX_EFFECTS_QUEUE_SIZE
) {

  @get:JvmName("distinctSavedStateModels")
  val models = savedStateHandle.getLiveData<M>(modelKey).distinctUntilChanged()

  // At the time of writing, `MobiusLoopViewModel` function doesn't provide a public function
  // to hook into model changes in the ViewModel. `onModelChanged` function is private.
  // Instead we are observing the `getModels` livedata to update the saved state handle.
  private val modelsObserver = Observer<M> {
    val currentSavedModel = savedStateHandle.get<M>(modelKey)
    if (currentSavedModel != it) {
      savedStateHandle[modelKey] = it
    }
  }

  init {
    getModels().distinctUntilChanged().observeForever(modelsObserver)
  }

  override fun onClearedInternal() {
    getModels().removeObserver(modelsObserver)
  }
}
