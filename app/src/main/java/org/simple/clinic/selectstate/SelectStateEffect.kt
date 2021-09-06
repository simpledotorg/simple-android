package org.simple.clinic.selectstate

import org.simple.clinic.appconfig.State

sealed class SelectStateEffect

object LoadStates : SelectStateEffect()

data class SaveSelectedState(val state: State) : SelectStateEffect()
