package org.simple.clinic.overdue.download.formatdialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class OpenAs : Parcelable

@Parcelize
data object Share : OpenAs()

@Parcelize
data object SharingInProgress : OpenAs()

@Parcelize
data object Download : OpenAs()
