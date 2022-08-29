package org.simple.clinic.scanid

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.INDIA_NHID_LENGTH
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientPrefillInfo
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.Found
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.NotFound
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.OtherError
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure
import org.simple.clinic.scanid.EnteredCodeValidationResult.Success
import org.simple.clinic.scanid.OpenedFrom.EditPatientScreen
import org.simple.clinic.scanid.OpenedFrom.InstantSearchScreen
import org.simple.clinic.scanid.OpenedFrom.PatientsTabScreen
import org.simple.clinic.scanid.ScanErrorState.IdentifierAlreadyExists
import java.util.UUID
import javax.inject.Inject

class ScanSimpleIdUpdate @Inject constructor(
    private val isIndianNHIDSupportEnabled: Boolean,
    private var isOnlinePatientLookupEnabled: Boolean
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
      is ScannedQRCodeJsonParsed -> scannedQRCodeParsed(model, event)
      InvalidQrCode -> invalidQrCode(model)
      is OnlinePatientLookupWithIdentifierCompleted -> onlinePatientLookupWithIdentifierCompleted(model, event)
      is CompleteMedicalRecordsSaved -> patientsFoundByOnlineLookup(event.completeMedicalRecords)
    }
  }

  private fun invalidQrCode(model: ScanSimpleIdModel): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return when (model.openedFrom) {
      is EditPatientScreen -> next(model.notSearching(), ShowScannedQrCodeError(ScanErrorState.InvalidQrCode))
      InstantSearchScreen, PatientsTabScreen -> next(model.notSearching().invalidQrCode())
    }
  }

  private fun onlinePatientLookupWithIdentifierCompleted(
      model: ScanSimpleIdModel,
      event: OnlinePatientLookupWithIdentifierCompleted
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return when (event.result) {
      is NotFound, is OtherError -> next(model.notSearching(), checkIfOpenedFromEditPatient(model, event.identifier))
      is Found -> patientIsFoundWhileSearchingFromOnlineLookup(model, event.result)
    }
  }

  private fun patientIsFoundWhileSearchingFromOnlineLookup(
      model: ScanSimpleIdModel,
      result: Found
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return when (model.openedFrom) {
      is EditPatientScreen -> next(model.notSearching(), ShowScannedQrCodeError(IdentifierAlreadyExists))
      InstantSearchScreen, PatientsTabScreen -> next(
          model.notSearching(), SaveCompleteMedicalRecords(result.medicalRecords)
      )
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

  private fun scannedQRCodeParsed(
      model: ScanSimpleIdModel,
      event: ScannedQRCodeJsonParsed
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return if (event.patientPrefillInfo != null && event.healthIdNumber != null) {
      searchPatientByIdentifierIfHealthIdNumberIsValid(event.patientPrefillInfo, event.healthIdNumber, model)
    } else {
      noChange()
    }
  }

  private fun searchPatientByIdentifierIfHealthIdNumberIsValid(
      patientPrefillInfo: PatientPrefillInfo,
      healthIdNumber: String,
      model: ScanSimpleIdModel
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> =
      if (healthIdNumberIsValid(healthIdNumber)) {
        val identifier = Identifier(healthIdNumber, IndiaNationalHealthId)
        next(model = model.searching().patientPrefillInfoChanged(patientPrefillInfo), SearchPatientByIdentifier(identifier))
      } else {
        next(model = model.notSearching().invalidQrCode())
      }

  private fun healthIdNumberIsValid(healthIdNumber: String): Boolean {
    return when (healthIdNumber.length) {
      INDIA_NHID_LENGTH -> true
      else -> false
    }
  }

  private fun patientSearchByIdentifierCompleted(
      model: ScanSimpleIdModel,
      event: PatientSearchByIdentifierCompleted
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return if (event.patients.isEmpty()) {
      searchPatientOnlineWhenOnlinePatientLookupEnabled(event, model)
    } else {
      when (model.openedFrom) {
        is EditPatientScreen -> next(model.notSearching(), ShowScannedQrCodeError(IdentifierAlreadyExists))
        InstantSearchScreen, PatientsTabScreen -> next(
            model = model.notSearching(),
            patientFoundByIdentifierSearch(patients = event.patients, identifier = event.identifier)
        )
      }
    }
  }

  private fun searchPatientOnlineWhenOnlinePatientLookupEnabled(
      event: PatientSearchByIdentifierCompleted,
      model: ScanSimpleIdModel
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val effect = if (isOnlinePatientLookupEnabled) {
      OnlinePatientLookupWithIdentifier(event.identifier)
    } else {
      checkIfOpenedFromEditPatient(model, event.identifier)
    }
    return dispatch(effect)
  }

  private fun checkIfOpenedFromEditPatient(
      model: ScanSimpleIdModel,
      identifier: Identifier
  ): ScanSimpleIdViewEffect {
    return if (model.openedFrom is EditPatientScreen)
      GoBackToEditPatientScreen(identifier)
    else
      OpenPatientSearch(identifier, null, model.patientPrefillInfo)
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
      BpPassport -> OpenPatientSearch(additionalIdentifier = null, initialSearchQuery = BpPassport.shortCode(identifier), patientPrefillInfo = null)
      else -> OpenPatientSearch(null, initialSearchQuery = identifier.value, null)
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
      searchPatientByNHID(model, clearInvalidQrCodeModel, event)
    }
  }

  private fun searchPatientByNHID(
      model: ScanSimpleIdModel,
      clearInvalidQrCodeModel: ScanSimpleIdModel,
      event: ScanSimpleIdScreenQrCodeScanned
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> =
      if (model.isOpenedFromEditPatientScreenToAddBpPassport) {
        next(model.notSearching(), ShowScannedQrCodeError(ScanErrorState.InvalidQrCode))
      } else {
        searchPatientWhenNHIDEnabled(clearInvalidQrCodeModel, event)
      }

  private fun searchPatientByBpPassport(
      event: ScanSimpleIdScreenQrCodeScanned,
      model: ScanSimpleIdModel,
      clearInvalidQrCodeModel: ScanSimpleIdModel
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    val bpPassportCode = UUID.fromString(event.text)
    val identifier = Identifier(bpPassportCode.toString(), BpPassport)

    return if (model.isOpenedFromEditPatientScreenToAddNhid) {
      next(model.notSearching(), ShowScannedQrCodeError(ScanErrorState.InvalidQrCode))
    } else {
      next(model = clearInvalidQrCodeModel.searching(), SearchPatientByIdentifier(identifier))
    }
  }

  private fun searchPatientWhenNHIDEnabled(
      model: ScanSimpleIdModel,
      event: ScanSimpleIdScreenQrCodeScanned
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return if (isIndianNHIDSupportEnabled) {
      searchPatientWithNhid(model, event)
    } else {
      noChange()
    }
  }

  private fun searchPatientWithNhid(
      model: ScanSimpleIdModel,
      event: ScanSimpleIdScreenQrCodeScanned
  ): Next<ScanSimpleIdModel, ScanSimpleIdEffect> {
    return next(model = model.searching(), ParseScannedJson(event.text))
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
    return OpenPatientSearch(additionalIdentifier = null, initialSearchQuery = enteredCodeToSearch, patientPrefillInfo = null)
  }
}
