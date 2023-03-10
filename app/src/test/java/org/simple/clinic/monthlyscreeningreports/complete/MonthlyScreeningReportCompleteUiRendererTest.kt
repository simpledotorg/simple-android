package org.simple.clinic.monthlyscreeningreports.complete

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class MonthlyScreeningReportCompleteUiRendererTest {

  private val ui = mock<MonthlyScreeningReportCompleteUi>()
  private val uiRenderer = MonthlyScreeningReportCompleteUiRenderer(ui)

  private val defaultModel = MonthlyScreeningReportCompleteModel.default()

  @Test
  fun `when questionnaire response is loaded, then render questionnaire response`() {
    // given

    val questionnaireResponse = TestData.questionnaireResponse(
        uuid = UUID.fromString("4145d792-7f97-43c1-908c-702b98d738c3"),
    )

    val model = defaultModel
        .questionnaireResponseLoaded(questionnaireResponse)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showCompletedDateTimeTextView(questionnaireResponse)
    verifyNoMoreInteractions(ui)
  }
}
