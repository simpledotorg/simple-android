package org.simple.clinic

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.util.Rules


class CanaryInstrumentationTest {

  @get:Rule
  val ruleChain: RuleChain = Rules.global()

  @Test
  fun testEnvironmentWorks() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    assertThat(context).isNotNull()
  }
}
