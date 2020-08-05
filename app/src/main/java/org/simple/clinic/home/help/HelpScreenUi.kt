package org.simple.clinic.home.help

interface HelpScreenUi : HelpScreenUiActions {
  fun showHelp(html: String)
  fun showNoHelpAvailable()
  fun showLoadingView()
  fun showNetworkErrorMessage()
  fun showUnexpectedErrorMessage()
}
