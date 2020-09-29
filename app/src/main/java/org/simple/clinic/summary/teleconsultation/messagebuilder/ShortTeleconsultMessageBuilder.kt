package org.simple.clinic.summary.teleconsultation.messagebuilder

import org.simple.clinic.summary.PatientTeleconsultationInfo
import javax.inject.Inject

class ShortTeleconsultMessageBuilder @Inject constructor() : TeleconsultationMessageBuilder {

  // TODO (SM): 2020-08-24 Change the deep link
  /**
   * We are not translating the message because we don't know the preferred language
   * for the recipient.
   */
  override fun message(patientTeleconsultationInfo: PatientTeleconsultationInfo): String {
    val message = StringBuilder("TELE-CONSULT")
        .appendLine()
        .appendLine()
        .appendLine("1. Review patient")
        .appendLine("2. Create log")
        .appendLine("https://app.simple.org/consult?r=${patientTeleconsultationInfo.teleconsultRecordId}&p=${patientTeleconsultationInfo.patientUuid}")
        .appendLine()

    return message.toString()
  }
}
