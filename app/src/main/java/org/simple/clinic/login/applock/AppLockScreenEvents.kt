package org.simple.clinic.login.applock

import org.simple.clinic.widgets.UiEvent

class AppLockScreenCreated : UiEvent

data class AppLockScreenPinTextChanged(val pin: String) : UiEvent

class AppLockScreenSubmitClicked : UiEvent

class AppLockScreenBackClicked : UiEvent

class LogoutClicked: UiEvent

class ForgotPinClicked: UiEvent
