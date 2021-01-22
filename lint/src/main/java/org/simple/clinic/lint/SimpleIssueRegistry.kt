package org.simple.clinic.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

@Suppress("UnstableApiUsage")
class SimpleIssueRegistry : IssueRegistry() {

  override val issues = listOf(
      ImageSrcDetector.ImageSrcIssue,
      CardViewDetector.CardViewIssue,
      TextViewTextSizeDetector.TextViewTextSizeIssue
  )

  override val api: Int
    get() = CURRENT_API
}
