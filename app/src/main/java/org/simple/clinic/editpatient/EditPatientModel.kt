package org.simple.clinic.editpatient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import java.time.format.DateTimeFormatter

@Parcelize
data class EditPatientModel(
    val savedEntry: EditablePatientEntry,
    val ongoingEntry: EditablePatientEntry,

    // TODO(rj): 2019-09-27 Do we really need these properties to update
    // patient information? Revisit these properties after migrating the feature
    val savedPatient: Patient,
    val savedAddress: PatientAddress,
    val savedPhoneNumber: PatientPhoneNumber?,
    val savedBangladeshNationalId: BusinessId?,
    val saveButtonState: EditPatientState?,
    val colonyOrVillagesList: List<String>?,
    val inputFields: InputFields?,
    val bpPassports: List<BusinessId>?,
    val isUserCountryIndia: Boolean
) : Parcelable {
  companion object {
    fun from(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?,
        dateOfBirthFormatter: DateTimeFormatter,
        bangladeshNationalId: BusinessId?,
        saveButtonState: EditPatientState?,
        isUserCountryIndia: Boolean
    ): EditPatientModel {
      val savedEntry = EditablePatientEntry.from(
          patient,
          address,
          phoneNumber,
          dateOfBirthFormatter,
          bangladeshNationalId
      )
      val ongoingEntry = savedEntry.copy()
      return EditPatientModel(
          savedEntry,
          ongoingEntry,
          patient,
          address,
          phoneNumber,
          bangladeshNationalId,
          saveButtonState,
          null,
          null,
          null,
          isUserCountryIndia
      )
    }
  }

  val hasColonyOrVillagesList: Boolean
    get() = !colonyOrVillagesList.isNullOrEmpty()

  val hasInputFields
    get() = inputFields != null

  fun updateName(name: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateName(name))

  fun updateGender(gender: Gender): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateGender(gender))

  fun updatePhoneNumber(phoneNumber: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updatePhoneNumber(phoneNumber))

  fun updateColonyOrVillage(colonyOrVillage: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateColonyOrVillage(colonyOrVillage))

  fun updateDistrict(district: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateDistrict(district))

  fun updateState(state: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateState(state))

  fun updateAge(age: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateAge(age))

  fun updateDateOfBirth(dateOfBirth: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateDateOfBirth(dateOfBirth))

  fun updateAlternativeId(alternativeId: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateAlternativeId(alternativeId))

  fun updateZone(zone: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateZone(zone))

  fun updateStreetAddress(streetAddress: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateStreetAddress(streetAddress))

  fun buttonStateChanged(buttonState: EditPatientState?): EditPatientModel =
      copy(saveButtonState = buttonState)

  fun updateColonyOrVillagesList(colonyOrVillages: List<String>): EditPatientModel =
      copy(colonyOrVillagesList = colonyOrVillages)

  fun inputFieldsLoaded(inputFields: InputFields): EditPatientModel {
    return copy(inputFields = inputFields)
  }

  fun bpPassportsLoaded(bpPassports: List<BusinessId>): EditPatientModel {
    return copy(bpPassports = bpPassports)
  }

  fun addBpPassports(bpPassports: List<Identifier>): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.addBpPassports(bpPassports))
}
