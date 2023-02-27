package org.simple.clinic.home.patients.links

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class PatientsTabLinkEffectHandlerTest {
  private val uiActions = mock<PatientsTabLinkUiActions>()

  private val questionnaireRepository = mock<QuestionnaireRepository>()

  private val questionnaireResponseRepository = mock<QuestionnaireResponseRepository>()

  private val effectHandler = PatientsTabLinkEffectHandler(
      questionnaireRepository = questionnaireRepository,
      questionnaireResponseRepository = questionnaireResponseRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when open patient line list download dialog effect is received, then open the dialog`() {
    // when
    effectHandlerTestCase.dispatch(OpenPatientLineListDownloadDialog)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).openPatientLineListDownloadDialog()
    verifyNoMoreInteractions(uiActions)
  }
}
