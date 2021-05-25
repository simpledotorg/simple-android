package org.simple.clinic.registration

sealed class SaveUserLocallyResult {

  class Success : SaveUserLocallyResult()

  class NetworkError : SaveUserLocallyResult()

  class UnexpectedError : SaveUserLocallyResult()
}
