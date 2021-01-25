package org.simple.clinic.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
class CardViewDetectorTest : LintDetectorTest() {

  fun `test using card view should throw error`() {
    lint()
        .files(
            xml("res/layout/card_view.xml", """
              <androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content">

              </androidx.cardview.widget.CardView>
            """.trimIndent())
        )
        .run()
        .expect("""
          res/layout/card_view.xml:1: Error: Please use MaterialCardView from Material library instead of CardView [CardViewUsage]
          <androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          1 errors, 0 warnings
        """)
  }

  fun `test using material card view shouldn't throw error`() {
    lint()
        .files(
            xml("res/layout/card_view.xml", """
              <com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content">

              </com.google.android.material.card.MaterialCardView>
            """.trimIndent())
        )
        .run()
        .expectClean()
  }

  override fun getDetector(): Detector {
    return CardViewDetector()
  }

  override fun getIssues(): List<Issue> {
    return listOf(CardViewDetector.CardViewIssue)
  }
}
