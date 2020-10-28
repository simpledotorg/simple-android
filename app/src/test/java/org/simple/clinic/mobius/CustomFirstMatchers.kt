package org.simple.clinic.mobius

import com.spotify.mobius.First
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.hamcrest.core.Is

object CustomFirstMatchers {
  fun <M, F> doesNotHaveEffectOfType(type: Class<*>): Matcher<First<M, F>> {

    return object : TypeSafeDiagnosingMatcher<First<M, F>>() {

      private val typeMatcher = Is.isA(type)

      override fun describeTo(
          description: Description
      ) {
        description
            .appendText("should not have effect matching: ")
            .appendDescriptionOf(typeMatcher)
      }

      override fun matchesSafely(
          item: First<M, F>,
          mismatchDescription: Description
      ): Boolean {
        val itemsMatching = item
            .effects()
            .filter(typeMatcher::matches)

        return if (itemsMatching.isNotEmpty()) {
          mismatchDescription
              .appendText("found effects: ")
              .appendValueList("[", ", ", "]", itemsMatching)
          false
        } else {
          true
        }
      }
    }
  }
}
