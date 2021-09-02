package org.simple.clinic.selectstate

import org.simple.clinic.appconfig.StatesResult

sealed class SelectStateEvent

data class StatesResultFetched(val result: StatesResult) : SelectStateEvent()

object StateSaved : SelectStateEvent()
