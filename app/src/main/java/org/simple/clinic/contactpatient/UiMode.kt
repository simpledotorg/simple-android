package org.simple.clinic.contactpatient

/**
 * This enum is used to render a different view in the
 * [ContactPatientBottomSheet] based on the user interaction.
 *
 * The reasons this was done instead of making the individual views are:
 *
 * - Avoid navigation issues between multiple bottom sheets.
 * - Make migrating this set of screens to the navigation component
 * easier. Once this is done, since nav component has support for
 * bottom sheet fragments, we can split these screens out again.
 * Mobius makes that pretty easy for us.
 **/
enum class UiMode {
  CallPatient,
  SetAppointmentReminder
}
