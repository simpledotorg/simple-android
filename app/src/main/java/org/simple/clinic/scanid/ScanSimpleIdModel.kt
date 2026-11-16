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
    val openedFrom: OpenedFrom
) : Parcelable {

  companion object {
    fun create(openedFrom: OpenedFrom) = ScanSimpleIdModel(enteredCode = null,
        scanSearchState = NotSearching,
        scanErrorState = null,
        openedFrom = openedFrom)
  }

  val isSearching: Boolean
    get() = scanSearchState == Searching

  val isOpenedFromEditPatientScreen
    get() = openedFrom is OpenedFrom.EditPatientScreen

  val isOpenedFromEditPatientScreenToAddBpPassport
    get() = openedFrom == OpenedFrom.EditPatientScreen.ToAddBpPassport

  val isOpenedFromEditPatientScreenToAddShrId
    get() = openedFrom == OpenedFrom.EditPatientScreen.ToAddShrId

  val canAcceptBpPassport
    get() = !isOpenedFromEditPatientScreen || isOpenedFromEditPatientScreenToAddBpPassport

  val canAcceptShrId
    get() = !isOpenedFromEditPatientScreen || isOpenedFromEditPatientScreenToAddShrId

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
}
