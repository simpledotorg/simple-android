package org.simple.clinic.scanid

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshSharedHealthRecordId.isValid
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.Found
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.NotFound
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.OtherError
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure
import org.simple.clinic.scanid.EnteredCodeValidationResult.Success
import org.simple.clinic.scanid.ScanErrorState.IdentifierAlreadyExists
import java.util.UUID
import javax.inject.Inject

class ScanSimpleIdUpdate @Inject constructor(
) : Update<ScanSimpleIdModel, ScanSimpleIdEvent, ScanSimpleIdEffect> {

  override fun update(
      model: ScanSimpleIdModel,
      event: ScanSimpleIdEvent
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return when (event) {
      ShowKeyboard -> dispatch(HideQrCodeScannerView)
      HideKeyboard -> dispatch(ShowQrCodeScannerView)
      EnteredCodeChanged -> next(model.clearInvalidQrCodeError(), HideEnteredCodeValidationError)
      is EnteredCodeValidated -> enteredCodeValidated(model, event)
      is EnteredCodeSearched -> next(model.enteredCodeChanged(event.enteredCode), ValidateEnteredCode(event.enteredCode))
      is ScanSimpleIdScreenQrCodeScanned -> simpleIdQrScanned(model, event)
      is PatientSearchByIdentifierCompleted -> patientSearchByIdentifierCompleted(model, event)
      InvalidQrCode -> invalidQrCode(model)
      is OnlinePatientLookupWithIdentifierCompleted -> onlinePatientLookupWithIdentifierCompleted(model, event)
      is CompleteMedicalRecordsSaved -> patientsFoundByOnlineLookup(event.completeMedicalRecords)
    }
  }

  private fun invalidQrCode(model: ScanSimpleIdModel): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return if (model.isOpenedToAddIdentifier) {
      next(model.notSearching(), ShowScannedQrCodeError(ScanErrorState.InvalidQrCode))
    } else {
      next(model.notSearching().invalidQrCode())
    }
  }

  private fun onlinePatientLookupWithIdentifierCompleted(
      model: ScanSimpleIdModel,
      event: OnlinePatientLookupWithIdentifierCompleted
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return when (event.result) {
      is NotFound, is OtherError -> next(model.notSearching(), checkIfOpenedFromEditPatient(model, event.identifier))
      is Found -> patientIsFoundWhileSearchingFromOnlineLookup(model, event.result, event.identifier)
    }
  }

  private fun patientIsFoundWhileSearchingFromOnlineLookup(
      model: ScanSimpleIdModel,
      result: Found,
      identifier: Identifier
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val isFoundOnlyOnPatientBeingEdited = result.medicalRecords.all { it.patient.patientUuid == model.patientUuidBeingEdited }

    return if (model.isOpenedToAddIdentifier && isFoundOnlyOnPatientBeingEdited) {
      next(model.notSearching(), checkIfOpenedFromEditPatient(model, identifier))
    } else if (model.isOpenedToAddIdentifier) {
      next(model.notSearching(), ShowScannedQrCodeError(IdentifierAlreadyExists))
    } else {
      next(model.notSearching(), SaveCompleteMedicalRecords(result.medicalRecords))
    }
  }

  private fun patientsFoundByOnlineLookup(completeMedicalRecords: List<CompleteMedicalRecord>): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val atLeastOneMedicalRecordFound = completeMedicalRecords.size > 1
    val effect = if (atLeastOneMedicalRecordFound) {
      multiplePatientsWithId(completeMedicalRecords.first().patient.businessIds.first().identifier)
    } else {
      val patientId = completeMedicalRecords.first().patient.patientUuid
      OpenPatientSummary(patientId)
    }
    return dispatch(effect)
  }

  private fun patientSearchByIdentifierCompleted(
      model: ScanSimpleIdModel,
      event: PatientSearchByIdentifierCompleted
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val isFoundOnlyOnPatientBeingEdited = model.isOpenedToAddIdentifier &&
        event.patients.all { it.uuid == model.patientUuidBeingEdited }

    return if (event.patients.isEmpty() || isFoundOnlyOnPatientBeingEdited) {
      searchPatientOnlineWhenOnlinePatientLookupEnabled(event)
    } else {
      if (model.isOpenedToAddIdentifier) {
        next(model.notSearching(), ShowScannedQrCodeError(IdentifierAlreadyExists))
      } else {
        next(
            model = model.notSearching(),
            patientFoundByIdentifierSearch(patients = event.patients, identifier = event.identifier)
        )
      }
    }
  }

  private fun searchPatientOnlineWhenOnlinePatientLookupEnabled(
      event: PatientSearchByIdentifierCompleted,
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return dispatch(OnlinePatientLookupWithIdentifier(event.identifier))
  }

  private fun checkIfOpenedFromEditPatient(
      model: ScanSimpleIdModel,
      identifier: Identifier
  ): ScanSimpleIdViewEffect {
    return if (model.isOpenedToAddIdentifier)
      GoBackToEditPatientScreen(identifier)
    else
      OpenPatientSearch(identifier, null)
  }

  private fun patientFoundByIdentifierSearch(
      patients: List<Patient>,
      identifier: Identifier
  ): ScanSimpleIdEffect {
    return if (patients.size > 1) {
      multiplePatientsWithId(identifier)
    } else {
      val patientId = patients.first().uuid
      OpenPatientSummary(patientId)
    }
  }

  private fun multiplePatientsWithId(identifier: Identifier): ScanSimpleIdEffect {
    return when (identifier.type) {
      BpPassport -> OpenPatientSearch(additionalIdentifier = null, initialSearchQuery = BpPassport.shortCode(identifier))
      else -> OpenPatientSearch(null, initialSearchQuery = identifier.value)
    }
  }

  private fun simpleIdQrScanned(
      model: ScanSimpleIdModel,
      event: ScanSimpleIdScreenQrCodeScanned
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    if (model.isSearching) return noChange()

    val clearInvalidQrCodeModel = model.clearInvalidQrCodeError()

    return try {
      searchPatientByBpPassport(event, model, clearInvalidQrCodeModel)
    } catch (e: Exception) {
      searchPatientBySharedHealthRecordId(event, model, clearInvalidQrCodeModel)
    }
  }

  private fun searchPatientBySharedHealthRecordId(
      event: ScanSimpleIdScreenQrCodeScanned,
      model: ScanSimpleIdModel,
      clearInvalidQrCodeModel: ScanSimpleIdModel
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val sharedHealthRecordId = extractSharedHealthRecordId(event.text)

    if (sharedHealthRecordId == null || !isValid(sharedHealthRecordId)) {
      return invalidQrCode(model)
    }

    val identifier = Identifier(sharedHealthRecordId, Identifier.IdentifierType.BangladeshSharedHealthRecordId)

    return if (model.canAcceptShrId) {
      next(model = clearInvalidQrCodeModel.searching(), SearchPatientByIdentifier(identifier))
    } else {
      next(model.notSearching(), ShowScannedQrCodeError(ScanErrorState.InvalidQrCode))
    }
  }

  private fun extractSharedHealthRecordId(scannedText: String): String? {
    return scannedText
        .lineSequence()
        .map(String::trim)
        .firstOrNull { isValid(it) }
  }

  private fun searchPatientByBpPassport(
      event: ScanSimpleIdScreenQrCodeScanned,
      model: ScanSimpleIdModel,
      clearInvalidQrCodeModel: ScanSimpleIdModel
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val bpPassportCode = UUID.fromString(event.text)
    val identifier = Identifier(bpPassportCode.toString(), BpPassport)

    return if (model.canAcceptBpPassport) {
      next(model = clearInvalidQrCodeModel.searching(), SearchPatientByIdentifier(identifier))
    } else {
      next(model.notSearching(), ShowScannedQrCodeError(ScanErrorState.InvalidQrCode))
    }
  }

  private fun enteredCodeValidated(
      model: ScanSimpleIdModel,
      event: EnteredCodeValidated
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val effect = when (event.result) {
      Success -> searchEnteredCode(model)
      is Failure -> ShowEnteredCodeValidationError(event.result)
    }

    return dispatch(effect)
  }

  private fun searchEnteredCode(model: ScanSimpleIdModel): ScanSimpleIdEffect {
    val enteredCodeToSearch = model.enteredCode!!.enteredCodeText
    return OpenPatientSearch(additionalIdentifier = null, initialSearchQuery = enteredCodeToSearch)
  }
}
