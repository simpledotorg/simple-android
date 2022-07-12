package org.simple.clinic.overdue.download.formatdialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

sealed class OpenAs : Parcelable

@Parcelize
data class Share(val selectedAppointmentIds: Set<UUID>) : OpenAs()

@Parcelize
data class SharingInProgress(val selectedAppointmentIds: Set<UUID>) : OpenAs()

@Parcelize
data class Download(val selectedAppointmentIds: Set<UUID>) : OpenAs()
