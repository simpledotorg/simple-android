package org.simple.clinic.summary.teleconsultation.sync

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import java.util.UUID
import javax.inject.Inject

class TeleconsultationFacilityRepositoryAndroidTest {

  @Inject
  lateinit var repository: TeleconsultationFacilityRepository

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    repository.clear()
  }

  @Test
  fun saving_records_should_work_correctly() {
    // given
    val facility1 = UUID.fromString("2866eef6-f7c5-402c-b453-dc55b9eeabe1")
    val facility2 = UUID.fromString("3d69ae6c-60fd-4d79-8533-4c4f7bce80a4")
    val facility3 = UUID.fromString("fd4f439f-4b1b-40fa-b3d3-86e7322c7bae")

    val medicalOfficer1 = TestData.medicalOfficer(
        id = UUID.fromString("9c9eac75-ce67-4f83-9544-1be117958329"),
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

    val teleconsultationFacilityWithMedicalOfficers = listOf(
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

    repository.save(teleconsultationFacilityWithMedicalOfficers).blockingAwait()

    // when
    val teleconsultationFacilities = repository.getAll()

    // then
    assertThat(teleconsultationFacilities).isEqualTo(teleconsultationFacilityWithMedicalOfficers)
  }

  @Test
  fun getting_medical_officers_should_work_correctly() {
    // given
    val facility1 = UUID.fromString("2866eef6-f7c5-402c-b453-dc55b9eeabe1")
    val facility2 = UUID.fromString("3d69ae6c-60fd-4d79-8533-4c4f7bce80a4")
    val facility3 = UUID.fromString("fd4f439f-4b1b-40fa-b3d3-86e7322c7bae")

    val medicalOfficer1 = TestData.medicalOfficer(
        id = UUID.fromString("8bcfa864-2441-40a4-87cf-b0f3847433ee"),
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

    val teleconsultationFacilityWithMedicalOfficers = listOf(
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

    repository.save(teleconsultationFacilityWithMedicalOfficers).blockingAwait()

    // when
    val medicalOfficers = repository.medicalOfficersForFacility(facility1)

    // then
    assertThat(medicalOfficers).isEqualTo(listOf(medicalOfficer1, medicalOfficer2))
  }
}
