package org.simple.clinic.help

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class HelpPullResult : Parcelable {

  @Parcelize
  data object Success : HelpPullResult()

  @Parcelize
  data object NetworkError : HelpPullResult()

  @Parcelize
  data object OtherError : HelpPullResult()
}
