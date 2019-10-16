package org.simple.clinic.mobius.migration

@Deprecated("""
  We are no longer using this approach to migrate towards Mobius.
  This package will be deleted as soon as Edit Patient feature becomes stable.""")
enum class Architecture(val analyticsName: String) {
  ORIGINAL("original"),
  MOBIUS("mobius")
}
