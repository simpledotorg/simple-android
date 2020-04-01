package org.simple.clinic.login

import io.reactivex.Completable

interface LoginOtpSmsListener {

  fun listenForLoginOtp(): Completable

  fun listenForLoginOtpBlocking()
}
