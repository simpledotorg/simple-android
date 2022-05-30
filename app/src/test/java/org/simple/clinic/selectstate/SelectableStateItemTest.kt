package org.simple.clinic.selectstate

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.sharedTestCode.TestData

class SelectableStateItemTest {

  private val andhraPradesh = TestData.state(displayName = "Andhra Pradesh")
  private val kerala = TestData.state(displayName = "Kerala")
  private val punjab = TestData.state(displayName = "Punjab")

  private val states = listOf(andhraPradesh, kerala, punjab)

  @Test
  fun `if the user hasn't selected a state, then none of list items should be selected`() {
    // when
    val listItems = SelectableStateItem.from(states = states, selectedState = null)

    // then
    assertThat(listItems)
        .containsExactly(
            SelectableStateItem(state = andhraPradesh, isStateSelectedByUser = false, showDivider = true),
            SelectableStateItem(state = kerala, isStateSelectedByUser = false, showDivider = true),
            SelectableStateItem(state = punjab, isStateSelectedByUser = false, showDivider = false)
        ).inOrder()
  }

  @Test
  fun `if the user has selected a state, then list item should be selected`() {
    // when
    val listItems = SelectableStateItem.from(states = states, selectedState = kerala)

    // then
    assertThat(listItems)
        .containsExactly(
            SelectableStateItem(state = andhraPradesh, isStateSelectedByUser = false, showDivider = true),
            SelectableStateItem(state = kerala, isStateSelectedByUser = true, showDivider = true),
            SelectableStateItem(state = punjab, isStateSelectedByUser = false, showDivider = false)
        ).inOrder()
  }

  @Test
  fun `if there is only one item in the list, then divider must not be shown`() {
    // when
    val listItems = SelectableStateItem.from(states = listOf(andhraPradesh), selectedState = andhraPradesh)

    // then
    assertThat(listItems)
        .containsExactly(
            SelectableStateItem(state = andhraPradesh, isStateSelectedByUser = true, showDivider = false)
        ).inOrder()
  }
}
