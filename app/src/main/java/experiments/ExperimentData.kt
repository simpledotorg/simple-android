package experiments

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.FacilityPayload
import org.simple.clinic.patient.Age
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
  private val phcAchare = FacilityPayload(
      uuid = UUID.fromString("f992fe9a-459e-4dff-87ac-88c50fe9d729"),
      name = "PHC Achare",
      facilityType = "PHC",
      streetAddress = "",
      villageOrColony = "",
      district = "Sindhudurg",
      state = "Maharashtra",
      country = "India",
      pinCode = "416614",
      protocolUuid = UUID.fromString("ad7d5e34-4d4a-4dcc-98dc-27e98977d1cc"),
      groupUuid = UUID.fromString("5ed7b8dd-1834-47d8-bed7-a93e476c8785"),
      locationLatitude = 16.188499,
      locationLongitude = 73.445007,
      createdAt = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
      deletedAt = null
  )
  private val phcAdeli = FacilityPayload(
      uuid = UUID.fromString("b12578cf-05b5-4082-ae4e-0c41a056ac03"),
      name = "PHC Adeli",
      facilityType = "PHC",
      streetAddress = "",
      villageOrColony = "",
      district = "Sindhudurg",
      state = "Maharashtra",
      country = "India",
      pinCode = "416516",
      protocolUuid = UUID.fromString("ad7d5e34-4d4a-4dcc-98dc-27e98977d1cc"),
      groupUuid = UUID.fromString("5ed7b8dd-1834-47d8-bed7-a93e476c8785"),
      locationLatitude = 16.181621,
      locationLongitude = 73.747786,
      createdAt = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
      deletedAt = null
  )
  private val phcAmboli = FacilityPayload(
      uuid = UUID.fromString("1f1960aa-255e-4705-888c-0e295814a28e"),
      name = "PHC Amboli",
      facilityType = "PHC",
      streetAddress = "",
      villageOrColony = "",
      district = "Sindhudurg",
      state = "Maharashtra",
      country = "India",
      pinCode = "416532",
      protocolUuid = UUID.fromString("ad7d5e34-4d4a-4dcc-98dc-27e98977d1cc"),
      groupUuid = UUID.fromString("5ed7b8dd-1834-47d8-bed7-a93e476c8785"),
      locationLatitude = 15.969499,
      locationLongitude = 74.00485,
      createdAt = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
      deletedAt = null
  )
  val facilityPayload = listOf(
      phcAchare,
      phcAdeli,
      phcAmboli
  )

  val loggedInUserPayload = LoggedInUserPayload(
      uuid = UUID.fromString("a9e10bef-0978-4363-9d4c-09587cb5805d"),
      fullName = "Test User",
      phoneNumber = "1111111111",
      pinDigest = "\$2a\$10\$HDDWyQS.SNtJ03QnubMBkeIDlLfpxXBQ0pgnuXmUvdtsSQVO4pgze",
      registrationFacilityId = phcAchare.uuid,
      status = UserStatus.DisapprovedForSyncing,
      createdAt = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt = Instant.parse("2018-01-01T00:00:00Z")
  )

  private fun createSeedDataRecord(
      patientUuid: UUID,
      addressUuid: UUID,
      patientName: String,
      patientGender: Gender,
      patientAge: Int,
      patientVillage: String,
      phoneNumberUuid: UUID?,
      patientPhoneNumber: String?,
      patientFacilityUuid: UUID,
      bpUuid: UUID
  ): SeedDataRecord {
    return SeedDataRecord(
        patient = Patient(
            uuid = patientUuid,
            addressUuid = addressUuid,
            fullName = patientName,
            gender = patientGender,
            dateOfBirth = null,
            status = PatientStatus.Active,
            createdAt = Instant.parse("2018-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            age = Age(value = patientAge, updatedAt = Instant.parse("2019-09-14T00:00:00Z")),
            recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
            deletedAt = null,
            syncStatus = SyncStatus.DONE
        ),
        address = PatientAddress(
            uuid = addressUuid,
            colonyOrVillage = patientVillage,
            district = "Sindhudurg",
            state = "Maharashtra",
            country = "India",
            createdAt = Instant.parse("2018-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            deletedAt = null
        ),
        phoneNumber = if (patientPhoneNumber != null) {
          PatientPhoneNumber(
              uuid = phoneNumberUuid!!,
              patientUuid = patientUuid,
              number = patientPhoneNumber,
              phoneType = PatientPhoneNumberType.Mobile,
              active = true,
              createdAt = Instant.parse("2018-01-01T00:00:00Z"),
              updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
              deletedAt = null
          )
        } else null,
        bloodPressureMeasurements = listOf(
            BloodPressureMeasurement(
                uuid = bpUuid,
                systolic = 120,
                diastolic = 80,
                syncStatus = SyncStatus.DONE,
                userUuid = loggedInUserPayload.uuid,
                facilityUuid = patientFacilityUuid,
                patientUuid = patientUuid,
                createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                deletedAt = null,
                recordedAt = Instant.parse("2018-01-01T00:00:00Z")
            )
        )
    )
  }

  val seedData = listOf(
      createSeedDataRecord(
          patientUuid = UUID.fromString("a2207fa2-0ca2-44af-af67-0db8d7bceeed"),
          addressUuid = UUID.fromString("a92b7cae-571a-43db-97ae-0ba70d9be37f"),
          patientName = "Priyanka Kulkarni",
          patientGender = Gender.Female,
          patientAge = 57,
          patientVillage = "Amboli",
          phoneNumberUuid = UUID.fromString("3ec41203-e7e9-42d5-a47c-7eb2a04a7934"),
          patientPhoneNumber = "9978991243",
          patientFacilityUuid = phcAchare.uuid,
          bpUuid = UUID.fromString("651bc05f-6f4c-4a91-950e-7d3a446a6fa4")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("68b19b12-ec56-487a-a728-e83c3e973439"),
          addressUuid = UUID.fromString("ad38cd86-ca29-4681-ab2c-01b22cf7a582"),
          patientName = "Priya Kulkarni",
          patientGender = Gender.Female,
          patientAge = 78,
          patientVillage = "Khandala",
          phoneNumberUuid = UUID.fromString("ae4bb7b8-b2d5-4f24-9f90-0fba8c0ffa7b"),
          patientPhoneNumber = "9222098712",
          patientFacilityUuid = phcAdeli.uuid,
          bpUuid = UUID.fromString("f09307d1-1cd7-4cdf-9605-671761961889")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("7ec6e506-f83e-43ea-ac1e-a49656371693"),
          addressUuid = UUID.fromString("3bc515d2-03df-400e-8c02-ce407b387b28"),
          patientName = "Preeti Hatwalne",
          patientGender = Gender.Female,
          patientAge = 55,
          patientVillage = "Malvan",
          phoneNumberUuid = UUID.fromString("8b9fde76-4845-4391-94ab-41704b08f26b"),
          patientPhoneNumber = "8789099911",
          patientFacilityUuid = phcAdeli.uuid,
          bpUuid = UUID.fromString("00418451-d5b1-465a-8717-57186aaabf76")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("bafa0e2c-7a4c-40a2-8034-a3e84c2e5534"),
          addressUuid = UUID.fromString("693b5fc8-1529-467f-afd9-52976b568dcb"),
          patientName = "Preeti Khanolkar",
          patientGender = Gender.Female,
          patientAge = 67,
          patientVillage = "Devli",
          phoneNumberUuid = UUID.fromString("29036511-99ad-4b29-b113-d7a611dcfa10"),
          patientPhoneNumber = "8789099912",
          patientFacilityUuid = phcAchare.uuid,
          bpUuid = UUID.fromString("95fe8b3a-b761-40a0-8f0b-158c27ddb87a")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("2d39703a-2cae-4c25-bc98-4ba1d6733dfa"),
          addressUuid = UUID.fromString("5ee5475b-c1f0-4448-af33-aad289f41fbe"),
          patientName = "Parag Kulkarni",
          patientGender = Gender.Male,
          patientAge = 71,
          patientVillage = "Achra",
          phoneNumberUuid = UUID.fromString("bf81bf4a-efa2-40d0-8266-0a9f745d9835"),
          patientPhoneNumber = "7878123476",
          patientFacilityUuid = phcAmboli.uuid,
          bpUuid = UUID.fromString("2ece5da3-fa06-4dbf-a7f1-4bb7b9ad71d4")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("baf833ac-eac9-4cd7-aa2e-d4299fe95cc5"),
          addressUuid = UUID.fromString("6773f028-b3b4-41c5-b5c8-43bfe5791b7e"),
          patientName = "Preeti Kulkarni",
          patientGender = Gender.Female,
          patientAge = 32,
          patientVillage = "Achra",
          phoneNumberUuid = UUID.fromString("476bede9-329c-4cfa-888a-95e49601d90e"),
          patientPhoneNumber = "7878123111",
          patientFacilityUuid = phcAchare.uuid,
          bpUuid = UUID.fromString("5dea4aec-73b8-488e-a295-5fa14b5c37da")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("e332c740-2625-4779-a5b0-94f8063663d1"),
          addressUuid = UUID.fromString("fb7839c0-d520-43ab-9f20-044b29ef8a3c"),
          patientName = "Preeti Naik",
          patientGender = Gender.Female,
          patientAge = 45,
          patientVillage = "Kudal",
          phoneNumberUuid = UUID.fromString("8a9cc4c2-15f3-4241-a08a-7b5d66a713bf"),
          patientPhoneNumber = "9978991223",
          patientFacilityUuid = phcAdeli.uuid,
          bpUuid = UUID.fromString("666fd1ba-9134-45d6-b58c-f5ef1ad02494")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("085f19a8-c5e8-46e9-af69-d5a3154ea440"),
          addressUuid = UUID.fromString("f2c3c593-a5b8-4416-a6bb-e008da9f59d5"),
          patientName = "Parag Naik",
          patientGender = Gender.Male,
          patientAge = 76,
          patientVillage = "Kudal",
          phoneNumberUuid = UUID.fromString("a4a7f9fb-1876-4c60-96cc-c71678928259"),
          patientPhoneNumber = "9978991200",
          patientFacilityUuid = phcAmboli.uuid,
          bpUuid = UUID.fromString("5469c389-00c9-4d38-a1ae-1f6c4cef7986")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("c574efba-aecf-4ecf-8641-5b4604e650de"),
          addressUuid = UUID.fromString("85734382-e358-4e32-af74-8f23ba78d0be"),
          patientName = "Priyanka Khanolkar",
          patientGender = Gender.Female,
          patientAge = 55,
          patientVillage = "Banda",
          phoneNumberUuid = UUID.fromString("bf73c70d-5f70-443d-9083-08139a3ecbd7"),
          patientPhoneNumber = "9118991200",
          patientFacilityUuid = phcAchare.uuid,
          bpUuid = UUID.fromString("27f9dd4b-370a-4f08-852f-2e87db22a65f")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("47c486da-3df2-4cbe-b4aa-a84ffcfe4a59"),
          addressUuid = UUID.fromString("3d3c5bc3-baea-4128-905d-41d2b99c66ce"),
          patientName = "Priyanka Joshi",
          patientGender = Gender.Female,
          patientAge = 61,
          patientVillage = "Vijaydurg",
          phoneNumberUuid = UUID.fromString("c68a3d2f-bd04-4378-9b44-33cee7e4d58e"),
          patientPhoneNumber = "7018991200",
          patientFacilityUuid = phcAmboli.uuid,
          bpUuid = UUID.fromString("8f29875a-112f-4563-89e3-5bcaf0b692dc")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("2a4f9c3e-390a-4f0d-ad67-32391be04c60"),
          addressUuid = UUID.fromString("1b8cea62-901d-4f53-895e-cdb99617a818"),
          patientName = "Parag Kumar",
          patientGender = Gender.Male,
          patientAge = 61,
          patientVillage = "Devgad",
          phoneNumberUuid = UUID.fromString("fc94ffbe-99ec-415e-9e84-badeff125047"),
          patientPhoneNumber = "8789011912",
          patientFacilityUuid = phcAdeli.uuid,
          bpUuid = UUID.fromString("dc4e016d-d757-4d70-a9c2-afffdf693938")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("f79df6c5-0426-4bf5-8b4d-f4302e533890"),
          addressUuid = UUID.fromString("62195574-b9a8-4941-be9d-648b9d9105f9"),
          patientName = "Priya Kumar",
          patientGender = Gender.Female,
          patientAge = 61,
          patientVillage = "Banda",
          phoneNumberUuid = UUID.fromString("2970b960-7500-42a3-ad4a-982fecc265d8"),
          patientPhoneNumber = "8789099212",
          patientFacilityUuid = phcAmboli.uuid,
          bpUuid = UUID.fromString("0eda32e8-802b-4a37-8a3f-c89236ebaddd")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("23163f44-127e-4965-bc0e-30a41c87fc70"),
          addressUuid = UUID.fromString("cb7a4628-eec0-491e-8874-3af40cabfcd8"),
          patientName = "Preeti Kumar",
          patientGender = Gender.Female,
          patientAge = 65,
          patientVillage = "Kankavli",
          phoneNumberUuid = UUID.fromString("8797d469-f3df-47ff-900b-4401cd5ccd5d"),
          patientPhoneNumber = "8789099111",
          patientFacilityUuid = phcAchare.uuid,
          bpUuid = UUID.fromString("6639c304-e17d-4128-87db-a5a1d1be6c67")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("72467979-dc30-4281-a7e8-8482df56d735"),
          addressUuid = UUID.fromString("6396b49c-eb74-4f6d-9b05-11e1fa845c4b"),
          patientName = "Priyanka Naik",
          patientGender = Gender.Female,
          patientAge = 54,
          patientVillage = "Kudal",
          phoneNumberUuid = null,
          patientPhoneNumber = null,
          patientFacilityUuid = phcAmboli.uuid,
          bpUuid = UUID.fromString("eb870017-41d3-424e-8dc0-f33e659515d2")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("95fd9e8d-712b-44f3-afa7-931a53250b08"),
          addressUuid = UUID.fromString("3b9e2d86-26e9-462e-83b0-0d69eaa16006"),
          patientName = "Prachi Kulkarni",
          patientGender = Gender.Female,
          patientAge = 56,
          patientVillage = "Devli",
          phoneNumberUuid = null,
          patientPhoneNumber = null,
          patientFacilityUuid = phcAdeli.uuid,
          bpUuid = UUID.fromString("906efbde-b388-44f2-96a0-da1de4c79aea")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("404e5ffc-f0f1-4d5c-bd63-6f8a8df7b97c"),
          addressUuid = UUID.fromString("c78dbebc-ef3b-4891-8a21-0b3c9e613bda"),
          patientName = "Prachi Hatwalne",
          patientGender = Gender.Female,
          patientAge = 54,
          patientVillage = "Banda",
          phoneNumberUuid = UUID.fromString("283f5bc4-2c86-45ec-8157-46979f3f4a34"),
          patientPhoneNumber = "8789099911",
          patientFacilityUuid = phcAmboli.uuid,
          bpUuid = UUID.fromString("d900af6c-901b-4d80-9d30-8e9c22aec87e")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("e646e03e-0b6b-4e1c-b193-533faba35a33"),
          addressUuid = UUID.fromString("3931ad57-f62e-40e0-9709-572fa74c0042"),
          patientName = "Prachi Hatwalne",
          patientGender = Gender.Female,
          patientAge = 65,
          patientVillage = "Achra",
          phoneNumberUuid = null,
          patientPhoneNumber = null,
          patientFacilityUuid = phcAchare.uuid,
          bpUuid = UUID.fromString("3ab345fb-51f4-442b-b9cd-99cbc8de86bf")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("2d64003f-4373-4c09-8c4e-dcd66712749e"),
          addressUuid = UUID.fromString("15034854-3ff7-49c6-8871-9d9ea956f13d"),
          patientName = "Prachi Hatwalne",
          patientGender = Gender.Female,
          patientAge = 76,
          patientVillage = "Malvan",
          phoneNumberUuid = null,
          patientPhoneNumber = null,
          patientFacilityUuid = phcAmboli.uuid,
          bpUuid = UUID.fromString("5d66a74e-8def-44fc-92fb-939f57dd7c23")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("88caf9f8-545e-4421-b3ac-7fb571471e78"),
          addressUuid = UUID.fromString("50ef8d1c-bbbc-4182-9b28-70341b9c58c8"),
          patientName = "Prachi Kumar",
          patientGender = Gender.Female,
          patientAge = 43,
          patientVillage = "Kudal",
          phoneNumberUuid = UUID.fromString("469652d3-d422-4600-a5cb-ee5fd0518845"),
          patientPhoneNumber = "7878123111",
          patientFacilityUuid = phcAdeli.uuid,
          bpUuid = UUID.fromString("4442c8cf-0fcb-4fc1-b1c9-7b69d32899bb")
      ),
      createSeedDataRecord(
          patientUuid = UUID.fromString("18c4e38c-97e1-48c4-a51d-bfa30429c4ad"),
          addressUuid = UUID.fromString("0f00589d-cc71-47fd-ba69-bc5a7d4b76b5"),
          patientName = "Prachi Khanolkar",
          patientGender = Gender.Female,
          patientAge = 65,
          patientVillage = "Amboli",
          phoneNumberUuid = UUID.fromString("8abf79e8-827b-4bff-81ca-bf22fc96808d"),
          patientPhoneNumber = "8789099912",
          patientFacilityUuid = phcAchare.uuid,
          bpUuid = UUID.fromString("4fb0bb30-f720-48b6-b2b0-5f6386883189")
      )
  )

  data class SeedDataRecord(
      val patient: Patient,
      val address: PatientAddress,
      val phoneNumber: PatientPhoneNumber?,
      val bloodPressureMeasurements: List<BloodPressureMeasurement>,
      val prescribedDrugs: List<PrescribedDrug> = emptyList()
  )
}
