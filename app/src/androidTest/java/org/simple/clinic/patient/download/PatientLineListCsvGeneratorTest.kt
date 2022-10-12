package org.simple.clinic.patient.download

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.businessid.Identifier
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.TestUserClock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class PatientLineListCsvGeneratorTest {

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var medicalHistoryRepository: MedicalHistoryRepository

  @Inject
  lateinit var bloodPressureRepository: BloodPressureRepository

  @Inject
  lateinit var userClock: TestUserClock

  @Inject
  lateinit var patientLineListCsvGenerator: PatientLineListCsvGenerator

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)

    userClock.setDate(LocalDate.parse("2018-04-15"))
  }

  @Test
  fun generating_csv_should_work_correctly() {
    // given
    val facilityId = UUID.fromString("c3087644-b2c8-4ecb-8210-7eae8cda789a")

    val facility = TestData.facility(
        uuid = facilityId,
        name = "PHC Obvious"
    )

    val patient1Id = UUID.fromString("308ff58b-40ee-480e-a254-182b9ceba9f0")
    val patient1AddressId = UUID.fromString("7fb7f2a8-997a-4d0c-91f6-d995bcf34f9f")

    val patient2Id = UUID.fromString("68163640-5ae9-40ae-a04a-7d414b8329f0")
    val patient2AddressId = UUID.fromString("d824cce4-3eef-433e-ba1d-135d600b665a")

    val patient1BusinessId1 = TestData.businessId(
        uuid = UUID.fromString("b39a57c2-6020-4109-bd4b-d426a0c4a890"),
        patientUuid = patient1Id,
        identifier = Identifier(
            value = "3e1f35cc-94dc-4cef-8796-ac2aa138dddd",
            type = Identifier.IdentifierType.BpPassport
        ),
        createdAt = Instant.parse("2017-12-01T00:00:00Z")
    )

    val patient1BusinessId2 = TestData.businessId(
        uuid = UUID.fromString("779881c7-c669-44b4-aae3-145263c7778d"),
        patientUuid = patient1Id,
        identifier = Identifier(
            value = "a84e4ef7-b330-4048-8936-944dc2fc46d1",
            type = Identifier.IdentifierType.BpPassport
        ),
        createdAt = Instant.parse("2018-01-01T00:00:00Z")
    )

    val patient2BusinessId1 = TestData.businessId(
        uuid = UUID.fromString("a455cbb1-4f76-4692-977e-ff81635d4645"),
        patientUuid = patient2Id,
        identifier = Identifier(
            value = "2725766f-d7fd-47ad-85b2-4500d1db01d1",
            type = Identifier.IdentifierType.BpPassport
        ),
        createdAt = Instant.parse("2018-01-01T00:00:00Z")
    )

    val patientProfile1 = TestData.patientProfile(
        patientName = "Ramesh Prasad",
        patientUuid = patient1Id,
        patientAddressUuid = patient1AddressId,
        generatePhoneNumber = false,
        patientPhoneNumber = "1111111111",
        generateBusinessId = false,
        businessIds = listOf(patient1BusinessId1, patient1BusinessId2),
        patientAddressStreet = "45 Marigold Lane",
        patientAddressColonyOrVillage = "Carroll Gardens",
        gender = Gender.Male,
        generateDateOfBirth = false,
        patientAgeDetails = PatientAgeDetails(
            ageValue = 65,
            ageUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            dateOfBirth = null
        ),
        patientRegisteredFacilityId = facilityId,
        patientAssignedFacilityId = facilityId,
        patientStatus = PatientStatus.Active,
        patientCreatedAt = Instant.parse("2017-01-01T00:00:00Z"),
        patientUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        patientDeletedAt = null
    )

    val patientProfile2 = TestData.patientProfile(
        patientName = "Pooja Kumari",
        patientUuid = patient2Id,
        patientAddressUuid = patient2AddressId,
        generatePhoneNumber = false,
        patientPhoneNumber = "6666666666",
        generateBusinessId = false,
        businessIds = listOf(patient2BusinessId1),
        patientAddressStreet = "45 Marigold Lane",
        patientAddressColonyOrVillage = "Carroll Gardens",
        gender = Gender.Female,
        generateDateOfBirth = false,
        patientAgeDetails = PatientAgeDetails(
            ageValue = 45,
            ageUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            dateOfBirth = null
        ),
        patientRegisteredFacilityId = facilityId,
        patientAssignedFacilityId = facilityId,
        patientStatus = PatientStatus.Dead,
        patientCreatedAt = Instant.parse("2017-01-01T00:00:00Z"),
        patientUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        patientDeletedAt = null
    )

    val patient1MedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("a30e9bcf-59a9-4f05-ba5a-3ac08b93adb1"),
        patientUuid = patient1Id,
        diagnosedWithHypertension = Answer.Yes,
        hasDiabetes = Answer.No
    )

    val patient2MedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("59f2cb85-e66a-498d-91b0-479ef2b36c3b"),
        patientUuid = patient2Id,
        diagnosedWithHypertension = Answer.Yes,
        hasDiabetes = Answer.Yes
    )

    val patient1Bp1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("5acbe694-2b40-4d7b-8ffa-0b2ebe27784f"),
        patientUuid = patient1Id,
        facilityUuid = facilityId,
        systolic = 140,
        diastolic = 95,
        createdAt = Instant.parse("2018-02-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-02-01T00:00:00Z"),
        recordedAt = Instant.parse("2018-02-01T00:00:00Z"),
        deletedAt = null
    )

    val patient1Bp2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("d2e77bc7-b342-43fe-96a3-4d46fefe2969"),
        patientUuid = patient1Id,
        facilityUuid = facilityId,
        systolic = 120,
        diastolic = 80,
        createdAt = Instant.parse("2018-03-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-03-01T00:00:00Z"),
        recordedAt = Instant.parse("2018-03-01T00:00:00Z"),
        deletedAt = null
    )

    val patient2Bp1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("05c669a4-bf09-4119-8b20-0be9aa8b31f8"),
        patientUuid = patient2Id,
        facilityUuid = facilityId,
        systolic = 140,
        diastolic = 95,
        createdAt = Instant.parse("2018-02-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-02-01T00:00:00Z"),
        recordedAt = Instant.parse("2018-02-01T00:00:00Z"),
        deletedAt = null
    )

    facilityRepository.save(listOf(facility))
    patientRepository.save(listOf(patientProfile1, patientProfile2))
    medicalHistoryRepository.save(listOf(patient1MedicalHistory, patient2MedicalHistory))
    bloodPressureRepository.save(listOf(patient1Bp1, patient1Bp2, patient2Bp1))

    // when
    val generatedCsv = patientLineListCsvGenerator.generate(
        facilityId = facilityId
    ).toString()

    // then
    val expectedCsv = """
      IHCI Register
      S.No,Name,Sex,Age,Registration date,Registration facility,Assigned facility,BP Passport,Street address,Village,Phone,Diagnosis,Jan-Mar Visit: HTN Controlled?,Status
      1,Ramesh Prasad,M,65,01/01/2017,PHC Obvious,,844 7330,45 Marigold Lane,Carroll Gardens,1111111111,HTN,Controlled,
      2,Pooja Kumari,F,45,01/01/2017,PHC Obvious,,272 5766,45 Marigold Lane,Carroll Gardens,6666666666,HTN + DM,-,Died

    """.trimIndent()

    assertThat(generatedCsv).isEqualTo(expectedCsv)
  }
}
