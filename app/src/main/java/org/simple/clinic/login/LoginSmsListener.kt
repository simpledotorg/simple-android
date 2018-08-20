package org.simple.clinic.login

import io.reactivex.Completable

interface LoginSmsListener {

  fun startListeningForLoginSms(): Completable
}
