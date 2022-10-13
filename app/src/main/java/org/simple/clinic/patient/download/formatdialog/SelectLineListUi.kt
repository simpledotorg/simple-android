package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.patient.download.PatientLineListFileFormat

interface SelectLineListUi {
  fun setLineListFormat(fileFormat: PatientLineListFileFormat)
}
