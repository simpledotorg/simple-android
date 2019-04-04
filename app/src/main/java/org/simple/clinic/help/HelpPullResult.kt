package org.simple.clinic.help

sealed class HelpPullResult {

  object Success: HelpPullResult()

  object NetworkError: HelpPullResult()

  object OtherError: HelpPullResult()
}
