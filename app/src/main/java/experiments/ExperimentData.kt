package experiments

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.FacilityPayload
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

object ExperimentData {
  val facilityPayload = listOf(
      FacilityPayload(
          uuid = UUID.fromString("f992fe9a-459e-4dff-87ac-88c50fe9d729"),
          name = "Facility 1",
          facilityType = "PHC",
          streetAddress = "",
          villageOrColony = "",
          district = "Bathinda",
          state = "Punjab",
          country = "India",
          pinCode = "",
          protocolUuid = UUID.fromString("ad7d5e34-4d4a-4dcc-98dc-27e98977d1cc"),
          groupUuid = UUID.fromString("5ed7b8dd-1834-47d8-bed7-a93e476c8785"),
          locationLatitude = 30.381528,
          locationLongitude = 74.978558,
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
          deletedAt = null
      ),
      FacilityPayload(
          uuid = UUID.fromString("b12578cf-05b5-4082-ae4e-0c41a056ac03"),
          name = "Facility 2",
          facilityType = "PHC",
          streetAddress = "",
          villageOrColony = "",
          district = "Bathinda",
          state = "Punjab",
          country = "India",
          pinCode = "",
          protocolUuid = UUID.fromString("ad7d5e34-4d4a-4dcc-98dc-27e98977d1cc"),
          groupUuid = UUID.fromString("5ed7b8dd-1834-47d8-bed7-a93e476c8785"),
          locationLatitude = 30.381528,
          locationLongitude = 74.978558,
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
          deletedAt = null
      )
  )

  val loggedInUserPayload = LoggedInUserPayload(
      uuid = UUID.fromString("a9e10bef-0978-4363-9d4c-09587cb5805d"),
      fullName = "Test User",
      phoneNumber = "1111111111",
      pinDigest = "\$2a\$10\$HDDWyQS.SNtJ03QnubMBkeIDlLfpxXBQ0pgnuXmUvdtsSQVO4pgze",
      registrationFacilityId = facilityPayload.first().uuid,
      status = UserStatus.DisapprovedForSyncing,
      createdAt = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt = Instant.parse("2018-01-01T00:00:00Z")
  )

  val seedData = listOf(
      (UUID.fromString("79b9ee8c-3c70-4428-830c-31a910864260") to UUID.fromString("8f886009-d662-443e-8859-6f48c18c671e"))
          .let { (patientUuid, addressUuid) ->
            SeedDataRecord(
                patient = Patient(
                    uuid = patientUuid,
                    addressUuid = addressUuid,
                    fullName = "Patient 1",
                    gender = Gender.Male,
                    dateOfBirth = LocalDate.parse("1990-01-01"),
                    status = PatientStatus.Active,
                    createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    age = null,
                    recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    deletedAt = null,
                    syncStatus = SyncStatus.DONE
                ),
                address = PatientAddress(
                    uuid = addressUuid,
                    colonyOrVillage = "Colony 1",
                    district = "District 1",
                    state = "State 1",
                    country = "India",
                    createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    deletedAt = null
                ),
                phoneNumber = PatientPhoneNumber(
                    uuid = UUID.fromString("42b288a7-788f-4175-bade-7fc863f9047d"),
                    patientUuid = patientUuid,
                    number = "1234567890",
                    phoneType = PatientPhoneNumberType.Mobile,
                    active = true,
                    createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    deletedAt = null
                ),
                bloodPressureMeasurements = listOf(
                    BloodPressureMeasurement(
                        uuid = UUID.fromString("c14cec20-7e29-456a-a5f1-21e528b08c59"),
                        systolic = 120,
                        diastolic = 80,
                        syncStatus = SyncStatus.DONE,
                        userUuid = loggedInUserPayload.uuid,
                        facilityUuid = facilityPayload.first().uuid,
                        patientUuid = patientUuid,
                        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                        deletedAt = null,
                        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
                    )
                ),
                prescribedDrugs = listOf(
                    PrescribedDrug(
                        uuid = UUID.fromString("7dc79948-a111-42b7-a917-a9367c4d1f24"),
                        name = "Drug 1",
                        dosage = "20 mg",
                        rxNormCode = null,
                        isDeleted = false,
                        isProtocolDrug = false,
                        patientUuid = patientUuid,
                        facilityUuid = facilityPayload.first().uuid,
                        syncStatus = SyncStatus.DONE,
                        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                        deletedAt = null
                    )
                )
            )
          },
      (UUID.fromString("465ed54a-691e-494d-98ef-5b7da641fa54") to UUID.fromString("7aa3cfd9-68ce-4a2b-819b-f846cf69047e"))
          .let { (patientUuid, addressUuid) ->
            SeedDataRecord(
                patient = Patient(
                    uuid = patientUuid,
                    addressUuid = addressUuid,
                    fullName = "Patient 2",
                    gender = Gender.Male,
                    dateOfBirth = LocalDate.parse("1985-01-01"),
                    status = PatientStatus.Active,
                    createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    age = null,
                    recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    deletedAt = null,
                    syncStatus = SyncStatus.DONE
                ),
                address = PatientAddress(
                    uuid = addressUuid,
                    colonyOrVillage = "Colony 2",
                    district = "District 2",
                    state = "State 2",
                    country = "India",
                    createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    deletedAt = null
                ),
                phoneNumber = PatientPhoneNumber(
                    uuid = UUID.fromString("26cffe4a-d2c0-4f70-b681-7e6ce3e075d1"),
                    patientUuid = patientUuid,
                    number = "0987654321",
                    phoneType = PatientPhoneNumberType.Mobile,
                    active = true,
                    createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    deletedAt = null
                ),
                bloodPressureMeasurements = listOf(
                    BloodPressureMeasurement(
                        uuid = UUID.fromString("32b21273-1a29-410f-a8ff-0aa98b1afc1b"),
                        systolic = 156,
                        diastolic = 98,
                        syncStatus = SyncStatus.DONE,
                        userUuid = loggedInUserPayload.uuid,
                        facilityUuid = facilityPayload.first().uuid,
                        patientUuid = patientUuid,
                        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                        deletedAt = null,
                        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
                    )
                ),
                prescribedDrugs = listOf(
                    PrescribedDrug(
                        uuid = UUID.fromString("c02f10ae-bc2a-486f-a829-02f92a83afc2"),
                        name = "Drug 2",
                        dosage = "40 mg",
                        rxNormCode = null,
                        isDeleted = false,
                        isProtocolDrug = false,
                        patientUuid = patientUuid,
                        facilityUuid = facilityPayload.first().uuid,
                        syncStatus = SyncStatus.DONE,
                        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                        deletedAt = null
                    )
                )
            )
          }
  )

  data class SeedDataRecord(
      val patient: Patient,
      val address: PatientAddress,
      val phoneNumber: PatientPhoneNumber?,
      val bloodPressureMeasurements: List<BloodPressureMeasurement>,
      val prescribedDrugs: List<PrescribedDrug>
  )
}
