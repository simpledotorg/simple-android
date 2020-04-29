package org.simple.clinic.router.screen

import android.os.Bundle
import android.os.Parcelable

/**
 * Helper class for saving a ViewGroup's state.
 */
data class ScreenSavedState(var superSavedState: Parcelable, var values: Bundle)
