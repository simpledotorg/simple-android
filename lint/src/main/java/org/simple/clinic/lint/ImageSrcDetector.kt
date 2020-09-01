package org.simple.clinic.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class ImageSrcDetector : ResourceXmlDetector() {

  companion object {
    const val ANDROID_URI = "http://schemas.android.com/apk/res/android"
    const val IMAGE_VIEW = "ImageView"
    const val ATTR_SRC = "src"

    private const val ImageSrcExplanation = "Please use `app:srcCompat` instead of `android:src` when setting an image resource"

    val ImageSrcIssue = Issue.create(
        id = "ImageViewSrc",
        briefDescription = "ImageView should not use `android:src`",
        explanation = ImageSrcExplanation,
        category = Category.CORRECTNESS,
        severity = Severity.WARNING,
        implementation = Implementation(
            ImageSrcDetector::class.java,
            Scope.RESOURCE_FILE_SCOPE
        ),
        androidSpecific = true
    )
  }

  override fun getApplicableElements() = listOf(IMAGE_VIEW)

  override fun visitElement(context: XmlContext, element: Element) {
    if (element.tagName == IMAGE_VIEW && element.hasAttributeNS(ANDROID_URI, ATTR_SRC)) {
      reportSrcIssue(context, element)
    }
  }

  private fun reportSrcIssue(context: XmlContext, element: Element) {
    context.report(
        ImageSrcIssue,
        element,
        context.getLocation(element),
        ImageSrcExplanation
    )
  }
}
