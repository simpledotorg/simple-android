package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.scanid.ScanSearchState.NotSearching
import org.simple.clinic.scanid.ScanSearchState.Searching

@Parcelize
data class ScanSimpleIdModel(
    val enteredCode: EnteredCodeInput?,
    val scanSearchState: ScanSearchState,
    val scanErrorState: ScanErrorState?,
    val patientPrefillInfo: PatientPrefillInfo?
) : Parcelable {

  companion object {
    fun create() = ScanSimpleIdModel(enteredCode = null,
        scanSearchState = NotSearching,
        scanErrorState = null,
        patientPrefillInfo = null)
  }

  val isSearching: Boolean
    get() = scanSearchState == Searching

  fun enteredCodeChanged(enteredCode: EnteredCodeInput): ScanSimpleIdModel {
    return copy(enteredCode = enteredCode)
  }

  fun searching(): ScanSimpleIdModel {
    return copy(scanSearchState = Searching)
  }

  fun notSearching(): ScanSimpleIdModel {
    return copy(scanSearchState = NotSearching)
  }

  fun invalidQrCode(): ScanSimpleIdModel {
    return copy(scanErrorState = ScanErrorState.InvalidQrCode)
  }

  fun clearInvalidQrCodeError(): ScanSimpleIdModel {
    return copy(scanErrorState = null)
  }
  
  fun patientPrefillInfoChanged(patientPrefillInfo: PatientPrefillInfo): ScanSimpleIdModel {
    return copy(patientPrefillInfo = patientPrefillInfo)
  }
}
