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
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.widgets.UiEvent

private const val MAX_EFFECTS_QUEUE_SIZE = 20

abstract class MobiusViewModel<M : Parcelable, E : UiEvent, F, V>(
  private val modelKey: String,
  private val savedStateHandle: SavedStateHandle,
  defaultModel: M,
  init: Init<M, F>? = null,
  loopFactoryProvider: (Consumer<V>) -> MobiusLoop.Builder<M, E, F>,
) : MobiusLoopViewModel<M, E, F, V>(
  /* loopFactoryProvider = */ loopFactoryProvider,
  /* modelToStartFrom = */ savedStateHandle.get<M>(modelKey) ?: defaultModel,
  /* init = */ init ?: Init { first(it) },
  /* mainLoopWorkRunner = */ MainThreadWorkRunner.create(),
  /* maxEffectQueueSize = */ MAX_EFFECTS_QUEUE_SIZE
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

  fun dispatch(event: E) {
    if (event.analyticsName.isNotBlank()) {
      Analytics.reportUserInteraction(event.analyticsName)
    }

    dispatchEvent(event)
  }

  override fun onClearedInternal() {
    getModels().removeObserver(modelsObserver)
  }
}
