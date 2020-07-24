package org.simple.clinic.login.pin

sealed class LoginPinEffect

object LoadOngoingLoginEntry : LoginPinEffect()
