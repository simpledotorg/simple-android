package org.simple.clinic.selectstate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appconfig.State

@Parcelize
data class SelectStateModel(
    val states: List<State>?
) : Parcelable {

  val hasStates
    get() = states != null

  companion object {

    fun create() = SelectStateModel(
        states = null
    )
  }

  fun statesLoaded(states: List<State>) = copy(states = states)
}
