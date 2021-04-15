package org.simple.clinic.help

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class HelpPullResult : Parcelable {

  @Parcelize
  object Success : HelpPullResult()

  @Parcelize
  object NetworkError : HelpPullResult()

  @Parcelize
  object OtherError : HelpPullResult()
}
