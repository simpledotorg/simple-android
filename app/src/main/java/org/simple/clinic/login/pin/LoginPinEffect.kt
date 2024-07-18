package org.simple.clinic.login.pin

import org.simple.clinic.user.OngoingLoginEntry

sealed class LoginPinEffect

data object LoadOngoingLoginEntry : LoginPinEffect()

data class SaveOngoingLoginEntry(val entry: OngoingLoginEntry) : LoginPinEffect()

data class LoginUser(val entry: OngoingLoginEntry) : LoginPinEffect()

data object ClearOngoingLoginEntry : LoginPinEffect()

sealed class LoginPinViewEffect : LoginPinEffect()

data object OpenHomeScreen : LoginPinViewEffect()

data object GoBackToRegistrationScreen : LoginPinViewEffect()
