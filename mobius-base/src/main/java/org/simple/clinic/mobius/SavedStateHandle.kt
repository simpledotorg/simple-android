package org.simple.clinic.mobius

import android.os.Bundle
import android.os.Parcelable

interface SavedStateHandle<M : Parcelable> {

  fun save(state: Parcelable?, model: M): Parcelable

  fun restore(state: Parcelable): Pair<M, Parcelable>
}

class ActivitySavedStateHandle<M : Parcelable>(
    private val modelKey: String
) : SavedStateHandle<M> {

  override fun save(state: Parcelable?, model: M): Parcelable {
    requireNotNull(state)

    val bundle = state as Bundle
    bundle.putParcelable(modelKey, model)

    return bundle
  }

  override fun restore(state: Parcelable): Pair<M, Parcelable> {
    val bundle = state as Bundle

    val model = bundle.getParcelable<M>(modelKey)
    requireNotNull(model)

    return model to bundle
  }
}
