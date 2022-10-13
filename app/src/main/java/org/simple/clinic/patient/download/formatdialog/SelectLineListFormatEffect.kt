package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.patient.download.PatientLineListFileFormat

sealed class SelectLineListFormatEffect

data class SchedulePatientLineListDownload(
    val fileFormat: PatientLineListFileFormat
) : SelectLineListFormatEffect()

sealed class SelectLineListFormatViewEffect : SelectLineListFormatEffect()

object Dismiss : SelectLineListFormatViewEffect()
