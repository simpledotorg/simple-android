package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.scanid.ScanSearchState.NotSearching
import org.simple.clinic.scanid.ScanSearchState.Searching

@Parcelize
data class ScanSimpleIdModel(
    val enteredCode: EnteredCodeInput?,
    val scanSearchState: ScanSearchState
) : Parcelable {

  companion object {
    fun create() = ScanSimpleIdModel(enteredCode = null, scanSearchState = NotSearching)
  }

  val isSearching: Boolean
    get() = scanSearchState == Searching

  fun shortCodeChanged(enteredCode: EnteredCodeInput): ScanSimpleIdModel {
    return copy(enteredCode = enteredCode)
  }

  fun searching(): ScanSimpleIdModel {
    return copy(scanSearchState = Searching)
  }

  fun notSearching(): ScanSimpleIdModel {
    return copy(scanSearchState = NotSearching)
  }
}
