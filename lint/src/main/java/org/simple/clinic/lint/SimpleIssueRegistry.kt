package org.simple.clinic.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class SimpleIssueRegistry : IssueRegistry() {

  override val issues = listOf(
      ImageSrcDetector.ImageSrcIssue,
      CardViewDetector.CardViewIssue,
      TextViewTextSizeDetector.TextViewTextSizeIssue
  )

  override val api: Int
    get() = CURRENT_API

  override val vendor: Vendor = Vendor(
      vendorName = "Simple.org",
      feedbackUrl = "https://github.com/simpledotorg/simple-android/issues"
  )
}
