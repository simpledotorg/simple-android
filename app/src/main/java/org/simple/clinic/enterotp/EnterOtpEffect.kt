package org.simple.clinic.enterotp

sealed class EnterOtpEffect

object LoadUser: EnterOtpEffect()

object ClearPin: EnterOtpEffect()

object TriggerSync: EnterOtpEffect()

object ClearLoginEntry: EnterOtpEffect()
