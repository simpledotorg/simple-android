package org.simple.clinic.util

import org.junit.rules.RuleChain

object Rules {

  fun global(): RuleChain = RuleChain
      .emptyRuleChain()
      .around(RxErrorsRule())
}
