package org.simple.clinic.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
class TextViewTextSizeDetectorTest : LintDetectorTest() {

  fun `test having text size in text view should throw error`() {
    lint()
        .files(
            xml("res/layout/text_view.xml", """
              <TextView xmlns:android="http://schemas.android.com/apk/res/android"
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content"
                  android:textSize="18sp"/>
            """.trimIndent())
        )
        .run()
        .expect("""
          res/layout/text_view.xml:4: Error: Please use android:textAppearance for setting the text style instead of setting the text size [TextViewTextSize]
              android:textSize="18sp"/>
              ~~~~~~~~~~~~~~~~~~~~~~~
          1 errors, 0 warnings
        """)
  }

  override fun getDetector(): Detector {
    return TextViewTextSizeDetector()
  }

  override fun getIssues(): List<Issue> {
    return listOf(TextViewTextSizeDetector.TextViewTextSizeIssue)
  }
}
