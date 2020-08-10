package org.simple.clinic.main

interface TheActivityUi: TheActivityUiActions {

  fun redirectToLogin()
  fun showAccessDeniedScreen(fullName: String)
}
