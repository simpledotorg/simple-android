package org.simple.clinic.home.help

interface HelpScreenUi {
  fun showHelp(html: String)
  fun showNoHelpAvailable()
  fun showLoadingView()
  fun showNetworkErrorMessage()
  fun showUnexpectedErrorMessage()
}
