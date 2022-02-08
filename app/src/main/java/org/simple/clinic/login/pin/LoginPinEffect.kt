package org.simple.clinic.login.pin

import org.simple.clinic.user.OngoingLoginEntry

sealed class LoginPinEffect

object LoadOngoingLoginEntry : LoginPinEffect()

data class SaveOngoingLoginEntry(val entry: OngoingLoginEntry) : LoginPinEffect()

data class LoginUser(val entry: OngoingLoginEntry) : LoginPinEffect()

object ClearOngoingLoginEntry : LoginPinEffect()

sealed class LoginPinViewEffect : LoginPinEffect()

object OpenHomeScreen : LoginPinViewEffect()

object GoBackToRegistrationScreen : LoginPinViewEffect()
