package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.patient.download.PatientLineListFileFormat

sealed class PatientLineListDownloadFormatEffect

data class SchedulePatientLineListDownload(
    val fileFormat: PatientLineListFileFormat
) : PatientLineListDownloadFormatEffect()

object Dismiss : PatientLineListDownloadFormatEffect()
