package org.simple.clinic.lint

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_SRC
import com.android.SdkConstants.ATTR_SRC_COMPAT
import com.android.SdkConstants.AUTO_URI
import com.android.SdkConstants.IMAGE_VIEW
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

class ImageSrcDetector : ResourceXmlDetector() {

  companion object {
    private const val ImageSrcExplanation = "Please use `app:srcCompat` instead of `android:src` when setting an image resource"

    val ImageSrcIssue = Issue.create(
        id = "ImageViewSrc",
        briefDescription = "ImageView should not use `android:src`",
        explanation = ImageSrcExplanation,
        category = Category.CORRECTNESS,
        severity = Severity.ERROR,
        implementation = Implementation(
            ImageSrcDetector::class.java,
            Scope.RESOURCE_FILE_SCOPE
        ),
        androidSpecific = true
    )
  }

  override fun getApplicableElements() = listOf(IMAGE_VIEW)

  override fun visitElement(context: XmlContext, element: Element) {
    if (element.hasAttributeNS(ANDROID_URI, ATTR_SRC)) {
      reportSrcIssue(context, element)
    }
  }

  private fun reportSrcIssue(context: XmlContext, element: Element) {
    context.report(
        ImageSrcIssue,
        element,
        context.getLocation(element.getAttributeNodeNS(ANDROID_URI, ATTR_SRC)),
        ImageSrcExplanation,
        LintFix.create().composite(
            LintFix.create().set(
                AUTO_URI, ATTR_SRC_COMPAT,
                element.getAttributeNS(ANDROID_URI, ATTR_SRC)
            ).build(),
            LintFix.create().unset(ANDROID_URI, ATTR_SRC).build()
        )
    )
  }
}
