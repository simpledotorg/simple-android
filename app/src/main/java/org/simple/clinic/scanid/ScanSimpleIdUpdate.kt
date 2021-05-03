package org.simple.clinic.scanid

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import com.squareup.moshi.Moshi
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure
import org.simple.clinic.scanid.EnteredCodeValidationResult.Success
import java.util.UUID
import javax.inject.Inject

class ScanSimpleIdUpdate @Inject constructor(
    private val crashReporter: CrashReporter,
    private val moshi: Moshi
) : Update<ScanSimpleIdModel, ScanSimpleIdEvent, ScanSimpleIdEffect> {
  override fun update(model: ScanSimpleIdModel, event: ScanSimpleIdEvent): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return when (event) {
      ShowKeyboard -> dispatch(HideQrCodeScannerView)
      HideKeyboard -> dispatch(ShowQrCodeScannerView)
      ShortCodeChanged -> dispatch(HideShortCodeValidationError)
      is EnteredCodeValidated -> shortCodeValidated(model, event)
      is EnteredCodeSearched -> next(model.shortCodeChanged(event.enteredCode), ValidateShortCode(event.enteredCode))
      is ScanSimpleIdScreenQrCodeScanned -> simpleIdQrScanned(model, event)
      is PatientSearchByIdentifierCompleted -> patientSearchByIdentifierCompleted(model, event)
    }
  }

  private fun patientSearchByIdentifierCompleted(
      model: ScanSimpleIdModel,
      event: PatientSearchByIdentifierCompleted
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val scanResult = if (event.patients.isNotEmpty()) {
      patientFoundByIdentifierSearch(patients = event.patients, identifier = event.identifier)
    } else {
      PatientNotFound(event.identifier)
    }

    return next(model = model.notSearching(), SendScannedIdentifierResult(scanResult))
  }

  private fun patientFoundByIdentifierSearch(patients: List<Patient>, identifier: Identifier): ScanResult {
    return if (patients.size > 1) {
      SearchByEnteredCode(BpPassport.shortCode(identifier)) //todo check if
    } else {
      val patientId = patients.first().uuid
      PatientFound(patientId)
    }
  }

  private fun simpleIdQrScanned(model: ScanSimpleIdModel, event: ScanSimpleIdScreenQrCodeScanned): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    if (model.isSearching) return noChange()

    return try {
      val patientPrefillInfoDetail = parseJsonIntoObject(event.text)
      if (patientPrefillInfoDetail != null) {
        val identifier = Identifier(patientPrefillInfoDetail.healthIdNumber, IndiaNationalHealthId)
        next(model = model.searching(), SearchPatientByIdentifier(identifier))
      } else {
        noChange()
      }
    } catch (e: Exception) {
      try {
        val bpPassportCode = UUID.fromString(event.text)
        val identifier = Identifier(bpPassportCode.toString(), BpPassport)
        next(model = model.searching(), SearchPatientByIdentifier(identifier))
      } catch (e: IllegalArgumentException) {
        crashReporter.report(e)
        noChange()
      }
    }
  }

  private fun parseJsonIntoObject(text: String): PatientPrefillInfo? {
    val adapter = moshi.adapter(PatientPrefillInfo::class.java)
    val patientPrefillInfoDetail: PatientPrefillInfo? = adapter.fromJson(text)
    return patientPrefillInfoDetail?.let {
      PatientPrefillInfo(
          it.healthIdNumber,
          it.healthIdUserName,
          it.fullName,
          it.gender,
          it.dateOfBirth,
          it.address,
          it.district,
          it.state,
      )
    }
  }

  private fun shortCodeValidated(model: ScanSimpleIdModel, event: EnteredCodeValidated): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val effect = when (event.result) {
      Success -> SendScannedIdentifierResult(SearchByEnteredCode(model.enteredCode!!.enteredCodeText))
      is Failure -> ShowShortCodeValidationError(event.result)
    }

    return dispatch(effect)
  }
}
