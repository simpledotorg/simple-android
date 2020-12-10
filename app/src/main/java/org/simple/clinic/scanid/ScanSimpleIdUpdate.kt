package org.simple.clinic.scanid

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure
import org.simple.clinic.scanid.ShortCodeValidationResult.Success
import java.util.UUID

class ScanSimpleIdUpdate : Update<ScanSimpleIdModel, ScanSimpleIdEvent, ScanSimpleIdEffect> {
  override fun update(model: ScanSimpleIdModel, event: ScanSimpleIdEvent): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return when (event) {
      ShowKeyboard -> dispatch(HideQrCodeScannerView)
      HideKeyboard -> dispatch(ShowQrCodeScannerView)
      ShortCodeChanged -> dispatch(HideShortCodeValidationError)
      is ShortCodeValidated -> shortCodeValidated(model, event)
      is ShortCodeSearched -> next(model.shortCodeChanged(event.shortCode), ValidateShortCode(event.shortCode))
      is ScanSimpleIdScreenQrCodeScanned -> simpleIdQrScanned(event)
      is PatientSearchByIdentifierCompleted -> patientSearchByIdentifierCompleted(event)
    }
  }

  private fun patientSearchByIdentifierCompleted(event: PatientSearchByIdentifierCompleted): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return if (event.patient.isPresent()) {
      val patientId = event.patient.get().uuid
      dispatch(SendScannedIdentifierResult(PatientFound(patientId)))
    } else {
      noChange()
    }
  }

  private fun simpleIdQrScanned(event: ScanSimpleIdScreenQrCodeScanned): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return try {
      val bpPassportCode = UUID.fromString(event.text)
      val identifier = Identifier(bpPassportCode.toString(), BpPassport)
      dispatch(SendScannedIdentifierResult(ScannedId(identifier)))
    } catch (e: IllegalArgumentException) {
      noChange()
    }
  }

  private fun shortCodeValidated(model: ScanSimpleIdModel, event: ShortCodeValidated): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val effect = when (event.result) {
      Success -> SendScannedIdentifierResult(EnteredShortCode(model.shortCode!!.shortCodeText))
      is Failure -> ShowShortCodeValidationError(event.result)
    }

    return dispatch(effect)
  }
}
