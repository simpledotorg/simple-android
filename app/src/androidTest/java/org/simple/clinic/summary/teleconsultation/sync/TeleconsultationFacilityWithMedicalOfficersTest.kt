package org.simple.clinic.summary.teleconsultation.sync

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import java.util.UUID
import javax.inject.Inject

class TeleconsultationFacilityWithMedicalOfficersTest {

  @Inject
  lateinit var teleconsultationFacilityDao: TeleconsultationFacilityInfo.RoomDao

  @Inject
  lateinit var medicalOfficerDao: MedicalOfficer.RoomDao

  @Inject
  lateinit var teleconsultationWithMedicalOfficerDao: TeleconsultationFacilityWithMedicalOfficers.RoomDao

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    teleconsultationFacilityDao.clear()
    medicalOfficerDao.clear()
    teleconsultationWithMedicalOfficerDao.clear()
  }

  @Test
  fun teleconsultation_facility_with_medical_officers_should_be_fetched_correctly() {
    // given
    val facility1 = UUID.fromString("2866eef6-f7c5-402c-b453-dc55b9eeabe1")
    val facility2 = UUID.fromString("3d69ae6c-60fd-4d79-8533-4c4f7bce80a4")
    val facility3 = UUID.fromString("fd4f439f-4b1b-40fa-b3d3-86e7322c7bae")

    val medicalOfficer1 = TestData.medicalOfficer(
        id = UUID.fromString("0f6ec14f-b28c-433b-b7d8-561ba843c640"),
        fullName = "Dr Sunil Dhar",
        phoneNumber = "1111111111"
    )

    val medicalOfficer2 = TestData.medicalOfficer(
        id = UUID.fromString("d4f33eef-c5e6-402c-b8c1-8c3f798b5ff0"),
        fullName = "Dr Abhishek Arora",
        phoneNumber = "2222222222"
    )

    val teleconsultationFacility1 = TestData.teleconsultationFacilityInfo(
        id = facility1,
        facilityId = facility1
    )

    val teleconsultationFacility2 = TestData.teleconsultationFacilityInfo(
        id = facility2,
        facilityId = facility2
    )

    val teleconsultationFacility3 = TestData.teleconsultationFacilityInfo(
        id = facility3,
        facilityId = facility3
    )

    val expectedTeleconsultationFacilityWithMedicalOfficers = listOf(
        TestData.teleconsultationFacilityWithMedicalOfficers(
            teleconsultationFacilityInfo = teleconsultationFacility1,
            medicalOfficers = listOf(medicalOfficer1, medicalOfficer2)
        ),
        TestData.teleconsultationFacilityWithMedicalOfficers(
            teleconsultationFacilityInfo = teleconsultationFacility2,
            medicalOfficers = listOf(medicalOfficer1)
        ),
        TestData.teleconsultationFacilityWithMedicalOfficers(
            teleconsultationFacilityInfo = teleconsultationFacility3,
            medicalOfficers = emptyList()
        )
    )

    teleconsultationFacilityDao.save(listOf(
        teleconsultationFacility1,
        teleconsultationFacility2,
        teleconsultationFacility3
    ))

    medicalOfficerDao.save(listOf(
        medicalOfficer1,
        medicalOfficer2
    ))

    teleconsultationWithMedicalOfficerDao.save(listOf(
        TestData.teleconsultationFacilityInfoMedicalOfficersCrossRef(
            teleconsultationFacilityUuid = facility1,
            medicalOfficerUuid = medicalOfficer1.medicalOfficerId
        ),
        TestData.teleconsultationFacilityInfoMedicalOfficersCrossRef(
            teleconsultationFacilityUuid = facility1,
            medicalOfficerUuid = medicalOfficer2.medicalOfficerId
        ),
        TestData.teleconsultationFacilityInfoMedicalOfficersCrossRef(
            teleconsultationFacilityUuid = facility2,
            medicalOfficerUuid = medicalOfficer1.medicalOfficerId
        )
    ))

    // when
    val teleconsultationFacilityWithMedicalOfficers = teleconsultationWithMedicalOfficerDao.getAll()

    // then
    assertThat(teleconsultationFacilityWithMedicalOfficers).isEqualTo(expectedTeleconsultationFacilityWithMedicalOfficers)
  }

  @Test
  fun fetching_a_single_teleconsultation_facility_with_medical_officers_should_work_correctly() {
    // given
    val facility1 = UUID.fromString("634d18ec-cb6f-4af7-aa59-7cc8aa0b96e1")
    val facility2 = UUID.fromString("f53a2e0c-ac23-4474-bcf7-5babd4e7b67f")

    val medicalOfficer1 = TestData.medicalOfficer(
        id = UUID.fromString("576f7a3c-3f61-4386-ad19-4f3012f12b6d"),
        fullName = "Dr Bhawana Sharma",
        phoneNumber = "1111111111"
    )

    val medicalOfficer2 = TestData.medicalOfficer(
        id = UUID.fromString("3b376ae3-098a-4120-a353-c8dea66177fb"),
        fullName = "Dr Supriya Gupta",
        phoneNumber = "2222222222"
    )

    val medicalOfficer3 = TestData.medicalOfficer(
        id = UUID.fromString("6941b750-de3d-4c03-98fe-632e7c205ef5"),
        fullName = "Dr Sunil Dhara",
        phoneNumber = "3333333333"
    )

    val teleconsultationFacility1 = TestData.teleconsultationFacilityInfo(
        id = facility1,
        facilityId = facility1
    )

    val teleconsultationFacility2 = TestData.teleconsultationFacilityInfo(
        id = facility2,
        facilityId = facility2
    )

    teleconsultationFacilityDao.save(listOf(
        teleconsultationFacility1,
        teleconsultationFacility2
    ))

    medicalOfficerDao.save(listOf(
        medicalOfficer1,
        medicalOfficer2,
        medicalOfficer3
    ))

    teleconsultationWithMedicalOfficerDao.save(listOf(
        TestData.teleconsultationFacilityInfoMedicalOfficersCrossRef(
            teleconsultationFacilityUuid = facility1,
            medicalOfficerUuid = medicalOfficer1.medicalOfficerId
        ),
        TestData.teleconsultationFacilityInfoMedicalOfficersCrossRef(
            teleconsultationFacilityUuid = facility1,
            medicalOfficerUuid = medicalOfficer2.medicalOfficerId
        ),
        TestData.teleconsultationFacilityInfoMedicalOfficersCrossRef(
            teleconsultationFacilityUuid = facility2,
            medicalOfficerUuid = medicalOfficer3.medicalOfficerId
        )
    ))

    // when
    val teleconsultationFacilityWithMedicalOfficers = teleconsultationWithMedicalOfficerDao.getOne(facilityId = facility2)

    // then
    assertThat(teleconsultationFacilityWithMedicalOfficers).isEqualTo(TestData.teleconsultationFacilityWithMedicalOfficers(
        teleconsultationFacilityInfo = teleconsultationFacility2,
        medicalOfficers = listOf(medicalOfficer3)
    ))
  }
}
