package org.simple.clinic.enterotp

sealed class EnterOtpEffect

object LoadUser: EnterOtpEffect()

object ClearPin: EnterOtpEffect()
