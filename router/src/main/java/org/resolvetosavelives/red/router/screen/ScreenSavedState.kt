package org.resolvetosavelives.red.router.screen

import android.os.Bundle
import android.os.Parcelable

/**
 * Helper class for saving a ViewGroup's state.
 * Usage: https://gitlab.corp.olacabs.com/olapartner/android/wikis/ui-navigation#5-saving-a-screens-state.
 */
data class ScreenSavedState(var superSavedState: Parcelable, var values: Bundle)
