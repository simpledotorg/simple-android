package org.simple.clinic.util

import com.vinaysshenoy.quarantine.QuarantineTestRule
import org.junit.rules.RuleChain

object Rules {

  private var quarantineClassLoader: ClassLoader = ClassLoader.getSystemClassLoader()

  fun overrideQuarantineClassloader(classLoader: ClassLoader) {
    quarantineClassLoader = classLoader
  }

  fun global(): RuleChain = RuleChain
      .emptyRuleChain()
      .around(QuarantineTestRule(quarantineClassLoader))
      .around(RxCompletableSubscribedRule())
      .around(RxErrorsRule())
}
