package org.simple.clinic.util

import com.vinaysshenoy.quarantine.QuarantineTestRule
import org.junit.rules.RuleChain

object Rules {

  fun global(): RuleChain = RuleChain
      .emptyRuleChain()
      .around(QuarantineTestRule())
      .around(RxErrorsRule())
}
