package org.simple.clinic.monthlyreports.list

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class MonthlyReportsUiRendererTest {

  private val ui = mock<MonthlyReportsUi>()
  private val uiRenderer = MonthlyReportsUiRenderer(ui)

  private val defaultModel = MonthlyReportsModel.default()

  @Test
  fun `when monthly reports is loaded, then render ui`() {
    // given
    val questionnaireResponse = TestData.questionnaireResponse(
        uuid = UUID.fromString("43d8c0b3-3341-4147-9a08-aac27e7f721f")
    )

    val model = defaultModel
        .monthlyReportsLoaded(listOf(questionnaireResponse))

    // when
    uiRenderer.render(model)

    // then
    verify(ui).displayMonthlyReports(listOf(questionnaireResponse))
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
