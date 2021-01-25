package org.simple.clinic.util.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class IterableNotContaining<F>(
    private val itemToTest: F
) : TypeSafeMatcher<Iterable<F>>() {

  companion object {
    fun <F> doesNotContain(itemToTest: F): Matcher<Iterable<F>> {
      return IterableNotContaining(itemToTest)
    }
  }

  override fun describeTo(description: Description) {
    description.appendText("does not contain: $itemToTest")
  }

  override fun matchesSafely(items: Iterable<F>): Boolean {
    return itemToTest !in items
  }
}
