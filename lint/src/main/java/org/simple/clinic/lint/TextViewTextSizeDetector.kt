package org.simple.clinic.lint

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_TEXT_SIZE
import com.android.SdkConstants.TEXT_VIEW
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class TextViewTextSizeDetector : ResourceXmlDetector() {

  companion object {
    private const val TextViewTextSizeExplanation = "Please use `android:textAppearance` for setting the text style instead of setting the text size"

    val TextViewTextSizeIssue = Issue.create(
        id = "TextViewTextSize",
        briefDescription = "Use `android:textAppearance` for applying the text style",
        explanation = TextViewTextSizeExplanation,
        category = Category.CORRECTNESS,
        severity = Severity.ERROR,
        implementation = Implementation(
            TextViewTextSizeDetector::class.java,
            Scope.RESOURCE_FILE_SCOPE
        ),
        androidSpecific = true
    )
  }

  override fun getApplicableElements() = listOf(TEXT_VIEW)

  override fun visitElement(context: XmlContext, element: Element) {
    if (element.hasAttributeNS(ANDROID_URI, ATTR_TEXT_SIZE)) {
      reportSrcIssue(context, element)
    }
  }

  private fun reportSrcIssue(context: XmlContext, element: Element) {
    context.report(
        TextViewTextSizeIssue,
        element,
        context.getLocation(element.getAttributeNodeNS(ANDROID_URI, ATTR_TEXT_SIZE)),
        TextViewTextSizeExplanation
    )
  }
}
