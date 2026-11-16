package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.scanid.ScanSearchState.NotSearching
import org.simple.clinic.scanid.ScanSearchState.Searching
import java.util.UUID

@Parcelize
data class ScanSimpleIdModel(
    val enteredCode: EnteredCodeInput?,
    val scanSearchState: ScanSearchState,
    val scanErrorState: ScanErrorState?,
    val openedFrom: OpenedFrom,
    val patientUuidBeingEdited: UUID?
) : Parcelable {

  companion object {
    fun create(
        openedFrom: OpenedFrom,
        patientUuidBeingEdited: UUID? = null
    ) = ScanSimpleIdModel(enteredCode = null,
        scanSearchState = NotSearching,
        scanErrorState = null,
        openedFrom = openedFrom,
        patientUuidBeingEdited = patientUuidBeingEdited)
  }

  val isSearching: Boolean
    get() = scanSearchState == Searching

  val isOpenedFromEditPatientScreen
    get() = openedFrom is OpenedFrom.EditPatientScreen

  val isOpenedFromEditPatientScreenToAddBpPassport
    get() = openedFrom == OpenedFrom.EditPatientScreen.ToAddBpPassport

  val isOpenedFromEditPatientScreenToAddShrId
    get() = openedFrom == OpenedFrom.EditPatientScreen.ToAddShrId

  val isOpenedFromPatientEntryScreen
    get() = openedFrom is OpenedFrom.PatientEntryScreen

  val isOpenedToAddIdentifier
    get() = isOpenedFromEditPatientScreen || isOpenedFromPatientEntryScreen

  val canAcceptBpPassport
    get() = !isOpenedToAddIdentifier || isOpenedFromEditPatientScreenToAddBpPassport

  val canAcceptShrId
    get() = !isOpenedToAddIdentifier ||
        isOpenedFromEditPatientScreenToAddShrId ||
        openedFrom == OpenedFrom.PatientEntryScreen.ToAddShrId

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
