package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport.SHORT_CODE_LENGTH
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshSharedHealthRecordId.isValid
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.scanid.EnteredCodeValidationResult.Success

@Parcelize
data class EnteredCodeInput(val enteredCodeText: String) : Parcelable {

  fun validate(): EnteredCodeValidationResult {
    return when {
      enteredCodeText.isEmpty() -> Empty
      enteredCodeText.length == SHORT_CODE_LENGTH || isValid(enteredCodeText) -> Success
      else -> NotEqualToRequiredLength
    }
  }
}

sealed class EnteredCodeValidationResult {
  data object Success : EnteredCodeValidationResult()

  sealed class Failure : EnteredCodeValidationResult() {
    data object Empty : Failure()
    data object NotEqualToRequiredLength : Failure()
  }
}
