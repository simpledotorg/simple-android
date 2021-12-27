package org.simple.clinic

import org.simple.clinic.login.LoginOtpSmsListener

class NoopSmsListenerOtp: LoginOtpSmsListener {

  override fun listenForLoginOtp() {
    // Nothing to do here
  }
}
