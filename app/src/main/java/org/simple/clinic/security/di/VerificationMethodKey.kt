package org.simple.clinic.security.di

import dagger.MapKey

@MapKey
annotation class VerificationMethodKey(val value: Method)

enum class Method {
  Local,
  OngoingEntry,
  LoginPinOnServer
}
