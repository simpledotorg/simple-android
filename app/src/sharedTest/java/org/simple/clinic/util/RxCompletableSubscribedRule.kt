package org.simple.clinic.util

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RxCompletableSubscribedRule: TestRule {

  override fun apply(base: Statement, description: Description): Statement {
    return SubscriptionTrackingStatement(base, description)
  }

  private class SubscriptionTrackingStatement(
      private val base: Statement,
      private val description: Description
  ) : Statement() {

    private val subscriptionTracker = RxJavaSubscriptionTracker()

    override fun evaluate() {

      subscriptionTracker.startTracking()

      try {
        base.evaluate()
      } finally {
        subscriptionTracker.stopTracking()
        subscriptionTracker.assertAllCompletablesSubscribed(description.getAnnotation(ExpectUnsubscribed::class.java))
      }
    }
  }
}
