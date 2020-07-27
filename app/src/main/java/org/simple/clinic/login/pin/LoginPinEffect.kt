package org.simple.clinic.login.pin

import org.simple.clinic.user.OngoingLoginEntry

sealed class LoginPinEffect

object LoadOngoingLoginEntry : LoginPinEffect()

data class SaveOngoingLoginEntry(val entry: OngoingLoginEntry) : LoginPinEffect()

data class LoginUser(val entry: OngoingLoginEntry) : LoginPinEffect()

object OpenHomeScreen : LoginPinEffect()

object GoBackToRegistrationScreen : LoginPinEffect()

object ClearOngoingLoginEntry : LoginPinEffect()
