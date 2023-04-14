package org.simple.clinic.monthlyreport.list

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class MonthlyReportListUiRendererTest {

  private val ui = mock<MonthlyReportListUi>()
  private val uiRenderer = MonthlyReportListUiRenderer(ui)

  private val defaultModel = MonthlyReportListModel.default()

  @Test
  fun `when questionnaire response list loaded, then render ui`() {
    // given
    val questionnaireResponse = TestData.questionnaireResponse(
        uuid = UUID.fromString("43d8c0b3-3341-4147-9a08-aac27e7f721f")
    )

    val model = defaultModel
        .responseListLoaded(listOf(questionnaireResponse))

    // when
    uiRenderer.render(model)

    // then
    verify(ui).displayMonthlyReportList(listOf(questionnaireResponse))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when current facility loaded, then set facility title`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("d05d20cf-858b-432a-a312-f402681e56b3"),
        name = "PHC Simple"
    )

    val model = defaultModel
        .currentFacilityLoaded(facility)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setFacility(facility.name)
    verifyNoMoreInteractions(ui)
  }
}
