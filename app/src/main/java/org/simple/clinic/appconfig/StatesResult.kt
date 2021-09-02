package org.simple.clinic.appconfig

import org.simple.clinic.util.ResolvedError

sealed class StatesResult {

  data class StatesFetched(val states: List<State>) : StatesResult()

  data class FetchError(val error: ResolvedError) : StatesResult()
}
