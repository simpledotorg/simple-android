package org.simple.clinic.patient.download.formatdialog

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.download.PatientLineListFileFormat
import org.simple.clinic.patient.download.PatientLineListScheduler

class SelectLineListFormatEffectHandlerTest {

  private val uiActions = mock<UiActions>()
  private val viewEffectHandler = SelectLineListFormatViewEffectHandler(uiActions)
  private val patientLineListScheduler = mock<PatientLineListScheduler>()
  private val effectHandler = SelectLineListFormatEffectHandler(
      patientLineListScheduler = patientLineListScheduler,
      viewEffectsConsumer = viewEffectHandler::handle
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @Test
  fun `when schedule patient line list download effect is received, then schedule download`() {
    // when
    testCase.dispatch(SchedulePatientLineListDownload(PatientLineListFileFormat.PDF))

    // then
    verify(patientLineListScheduler).schedule(PatientLineListFileFormat.PDF)
    verifyNoMoreInteractions(patientLineListScheduler)
  }

  @Test
  fun `when dismiss effect is received, then dismiss the dialog`() {
    // when
    testCase.dispatch(Dismiss)

    // then
    verify(uiActions).dismiss()
    verifyNoMoreInteractions(uiActions)
  }
}
