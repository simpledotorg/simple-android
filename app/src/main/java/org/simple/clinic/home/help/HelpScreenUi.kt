package org.simple.clinic.home.help

interface HelpScreenUi {
  fun showHelp(html: String)
  fun showNoHelpAvailable()
  fun showNetworkErrorMessage()
  fun showUnexpectedErrorMessage()
}
