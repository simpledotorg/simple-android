package org.simple.clinic

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.vinaysshenoy.quarantine.Quarantine
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.util.Rules
import kotlin.random.Random


class CanaryInstrumentationTest {

  @get:Rule
  val ruleChain: RuleChain = Rules.global()

  @Test
  fun testEnvironmentWorks() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    assertThat(context).isNotNull()
  }

  @Test
  fun test_that_30p_flaky_test_is_captured() {
    if (Quarantine.isEnabled) {
      assertThat(Random.nextFloat()).isGreaterThan(0.3F)
    }
  }

  @Test
  fun test_that_70p_flaky_test_is_captured() {
    if (Quarantine.isEnabled) {
      assertThat(Random.nextFloat()).isGreaterThan(0.7F)
    }
  }
}
