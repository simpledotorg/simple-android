package org.simple.clinic.lint

import com.android.SdkConstants.CARD_VIEW
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class CardViewDetector : ResourceXmlDetector() {

  companion object {
    private const val CardViewExplanation = "Please use `MaterialCardView` from Material library instead of `CardView`"

    val CardViewIssue = Issue.create(
        id = "CardViewUsage",
        briefDescription = "Use `MaterialCardView`",
        explanation = CardViewExplanation,
        category = Category.CORRECTNESS,
        severity = Severity.ERROR,
        implementation = Implementation(
            CardViewDetector::class.java,
            Scope.RESOURCE_FILE_SCOPE
        ),
        androidSpecific = true
    )
  }

  override fun getApplicableElements() = listOf(CARD_VIEW.newName(), CARD_VIEW.oldName())

  override fun visitElement(context: XmlContext, element: Element) {
    reportCardViewIssue(context, element)
  }

  private fun reportCardViewIssue(context: XmlContext, element: Element) {
    context.report(
        CardViewIssue,
        context.getElementLocation(element),
        CardViewExplanation
    )
  }
}
