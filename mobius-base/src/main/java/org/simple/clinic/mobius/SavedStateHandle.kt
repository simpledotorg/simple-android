package org.simple.clinic.mobius

import android.os.Parcelable

interface SavedStateHandle {

  fun save(state: Parcelable?): Parcelable

  fun restore(state: Parcelable): Parcelable
}
