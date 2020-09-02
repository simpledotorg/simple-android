package org.simple.clinic.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
class ImageSrcDetectorTest : LintDetectorTest() {

  fun `test having src in image view should throw warning`() {
    lint()
        .files(
            xml("res/layout/image_view.xml", """
              <ImageView xmlns:android="http://schemas.android.com/apk/res/android"
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content"
                  android:src="@drawable/ic_done"/>
            """.trimIndent())
        )
        .run()
        .expect("""
          res/layout/image_view.xml:4: Error: Please use app:srcCompat instead of android:src when setting an image resource [ImageViewSrc]
              android:src="@drawable/ic_done"/>
              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          1 errors, 0 warnings
        """)
  }

  fun `test having srcCompat in image view should not throw warning`() {
    lint()
        .files(
            xml("res/layout/image_view.xml", """
              <ImageView xmlns:android="http://schemas.android.com/apk/res/android"
                  xmlns:app="http://schemas.android.com/apk/res-auto"
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content"
                  app:srcCompat="@drawable/ic_done"/>
            """.trimIndent())
        )
        .run()
        .expectClean()
  }

  override fun getDetector(): Detector {
    return ImageSrcDetector()
  }

  override fun getIssues(): List<Issue> {
    return listOf(ImageSrcDetector.ImageSrcIssue)
  }
}
