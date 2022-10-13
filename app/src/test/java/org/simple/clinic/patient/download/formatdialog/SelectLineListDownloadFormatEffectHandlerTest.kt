package org.simple.clinic.patient.download.formatdialog

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.download.PatientLineListFileFormat
import org.simple.clinic.patient.download.PatientLineListScheduler

class SelectLineListDownloadFormatEffectHandlerTest {

  @Test
  fun `when schedule patient line list download effect is received, then schedule download`() {
    // given
    val patientLineListScheduler = mock<PatientLineListScheduler>()
    val effectHandler = SelectLineListDownloadFormatEffectHandler(
        patientLineListScheduler = patientLineListScheduler
    )
    val testCase = EffectHandlerTestCase(effectHandler.build())

    // when
    testCase.dispatch(SchedulePatientLineListDownload(PatientLineListFileFormat.PDF))

    // then
    verify(patientLineListScheduler).schedule(PatientLineListFileFormat.PDF)
    verifyNoMoreInteractions(patientLineListScheduler)
  }
}
