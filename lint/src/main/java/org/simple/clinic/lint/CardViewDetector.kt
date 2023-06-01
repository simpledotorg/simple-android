package org.simple.clinic.lint

import com.android.SdkConstants.ANDROIDX_CARD_VIEW_PKG
import com.android.SdkConstants.ANDROID_SUPPORT_V7_PKG
import com.android.SdkConstants.CARD_VIEW_LIB_ARTIFACT
import com.android.support.AndroidxName
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

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

  override fun getApplicableElements() = listOf(
      "android.support.v7.widget.CardView",
      "androidx.cardview.widget.CardView"
  )

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
