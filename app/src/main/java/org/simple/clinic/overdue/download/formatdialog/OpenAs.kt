package org.simple.clinic.overdue.download.formatdialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class OpenAs : Parcelable

@Parcelize
object Share : OpenAs()

@Parcelize
object SharingInProgress : OpenAs()

@Parcelize
object Download : OpenAs()
