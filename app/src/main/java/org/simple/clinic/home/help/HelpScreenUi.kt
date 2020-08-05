package org.simple.clinic.home.help

interface HelpScreenUi : HelpScreenUiActions {
  fun showHelp(html: String)
  fun showNoHelpAvailable()
  fun showNetworkErrorMessage()
  fun showUnexpectedErrorMessage()
}
