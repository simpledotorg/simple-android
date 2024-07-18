package org.simple.clinic.selectstate

import org.simple.clinic.appconfig.State

sealed class SelectStateEffect

data object LoadStates : SelectStateEffect()

data class SaveSelectedState(val state: State) : SelectStateEffect()

sealed class SelectStateViewEffect : SelectStateEffect()

data object GoToRegistrationScreen : SelectStateViewEffect()

data object ReplaceCurrentScreenWithRegistrationScreen : SelectStateViewEffect()
