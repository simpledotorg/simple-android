package org.simple.clinic.summary

object SummaryListAdapterIds {
  const val ADD_PRESCRIPTION = -2L
  const val ADD_NEW_BP = -3L

  // We can have upto three placeholders (might change in the future) so we can't hardcode an id.
  // At the same time, we we have to choose IDs that will stop it from clashing with other IDs in the
  // adapter, so we increment the placeholder number from a particular offset.
  val BP_PLACEHOLDER = { placeholderNumber: Int -> -(placeholderNumber + 100L) }
}
