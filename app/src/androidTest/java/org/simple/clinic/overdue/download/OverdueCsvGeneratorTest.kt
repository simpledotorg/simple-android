package org.simple.clinic.overdue.download

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.OverdueCsvTitleDateTime
import org.simple.clinic.di.DateFormatter.Type.OverduePatientRegistrationDate
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.businessid.Identifier
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.TestUserClock
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class OverdueCsvGeneratorTest {

  @Inject
  lateinit var userClock: TestUserClock

  @Inject
  lateinit var utcClock: TestUtcClock

  @Inject
  @DateFormatter(OverdueCsvTitleDateTime)
  lateinit var overdueCsvTitleDateTimeFormatter: DateTimeFormatter

  @Inject
  @DateFormatter(OverduePatientRegistrationDate)
  lateinit var patientRegistrationDateFormatter: DateTimeFormatter

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var appointmentRepository: AppointmentRepository

  @Inject
  lateinit var prescriptionRepository: PrescriptionRepository

  @Inject
  lateinit var appDatabase: AppDatabase

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)

    userClock.setDate(LocalDate.parse("2018-01-01"))
    utcClock.setDate(LocalDate.parse("2018-01-01"))

    appDatabase.clearAppData()
  }

  @Test
  fun generating_csv_should_work_correctly() {
    // given
    val csvGenerator = OverdueCsvGenerator(
        userClock = userClock,
        utcClock = utcClock,
        overdueCsvTitleDateTimeFormatter = overdueCsvTitleDateTimeFormatter,
        patientRegistrationDateFormatter = patientRegistrationDateFormatter,
        appointmentRepository = appointmentRepository
    )

    val patient1Uuid = UUID.fromString("714ea934-03c5-4d5f-9982-4f3b9f297622")
    val patient2Uuid = UUID.fromString("4788154c-ebf3-4c0e-9141-668896351edc")
    val patient3Uuid = UUID.fromString("73a2d3f8-1ca4-4c5a-9e6e-a3b99142643d")

    val patient1 = TestData.patientProfile(
        patientUuid = patient1Uuid,
        generatePhoneNumber = false,
        patientPhoneNumber = "1111111111",
        generateBusinessId = false,
        businessId = TestData.businessId(
            patientUuid = patient1Uuid,
            identifier = Identifier(
                value = "773d3caf-91f8-47f0-8766-45cf9cefd743",
                type = Identifier.IdentifierType.BpPassport
            )
        ),
        gender = Gender.Male,
        patientName = "Ramesh Murthy",
        patientAgeDetails = PatientAgeDetails(
            ageValue = 47,
            ageUpdatedAt = Instant.parse("2017-02-01T00:00:00Z"),
            dateOfBirth = null
        ),
        patientCreatedAt = Instant.parse("2017-02-01T00:00:00Z"),
        patientAddressStreet = "Bhamini Burg",
        patientAddressColonyOrVillage = "68691 Sarvin Drive"
    )
    val patient2 = TestData.patientProfile(patientUuid = patient2Uuid)
    val patient3 = TestData.patientProfile(
        patientUuid = patient3Uuid,
        generatePhoneNumber = false,
        patientPhoneNumber = "2222222222",
        generateBusinessId = false,
        businessId = TestData.businessId(
            patientUuid = patient3Uuid,
            identifier = Identifier(
                value = "08b2a0fb-fe5d-47fd-ad73-2a8b594a3621",
                type = Identifier.IdentifierType.BpPassport
            )
        ),
        gender = Gender.Female,
        patientName = "Shreya Mishra",
        patientAgeDetails = PatientAgeDetails(
            ageValue = null,
            ageUpdatedAt = Instant.parse("2017-01-01T00:00:00Z"),
            dateOfBirth = LocalDate.parse("1987-01-01")
        ),
        patientCreatedAt = Instant.parse("2017-01-01T00:00:00Z"),
        patientAddressStreet = "Enakshi Burg",
        patientAddressColonyOrVillage = "18473 Vaishnavi Curve"
    )

    val appointmentForPatient1UUID = UUID.fromString("13ad6674-ab3a-454a-aa12-a827cef374b0")
    val appointmentForPatient2UUID = UUID.fromString("01472302-001d-4ac5-b93e-9477e3a09032")
    val appointmentForPatient3UUID = UUID.fromString("a6ec46c1-2715-41fb-a60e-d5ccb8851526")

    val appointmentForPatient1 = TestData.appointment(
        uuid = appointmentForPatient1UUID,
        patientUuid = patient1Uuid,
        status = Appointment.Status.Scheduled,
        scheduledDate = LocalDate.parse("2017-12-01"),
        cancelReason = null,
        appointmentType = Appointment.AppointmentType.Manual,
        syncStatus = SyncStatus.DONE
    )
    val appointmentForPatient2 = TestData.appointment(
        uuid = appointmentForPatient2UUID,
        patientUuid = patient2Uuid,
        status = Appointment.Status.Cancelled,
        cancelReason = AppointmentCancelReason.PatientNotResponding,
        appointmentType = Appointment.AppointmentType.Manual,
        syncStatus = SyncStatus.DONE
    )
    val appointmentForPatient3 = TestData.appointment(
        uuid = appointmentForPatient3UUID,
        patientUuid = patient3Uuid,
        status = Appointment.Status.Cancelled,
        scheduledDate = LocalDate.parse("2017-12-07"),
        cancelReason = AppointmentCancelReason.InvalidPhoneNumber,
        appointmentType = Appointment.AppointmentType.Automatic,
        syncStatus = SyncStatus.PENDING
    )

    val prescribedDrugsForPatient1 = listOf(
        TestData.prescription(
            uuid = UUID.fromString("96a7c808-1f36-42db-8083-346f7b8726d3"),
            name = "Amlodipine",
            dosage = "25 mg",
            isDeleted = false,
            patientUuid = patient1Uuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("31d1c637-21dc-4453-8c78-10c1a5405523"),
            name = "Atenolol",
            dosage = "25 mg",
            isDeleted = false,
            patientUuid = patient1Uuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("e2350bf8-c2ab-4461-9708-f00cf2133453"),
            name = "Aspirin",
            dosage = "15 mg",
            isDeleted = true,
            patientUuid = patient1Uuid
        )
    )

    val prescribedDrugsForPatient2 = listOf(
        TestData.prescription(
            uuid = UUID.fromString("cdc5569d-2572-4753-b5c2-951d13a1a84a"),
            patientUuid = patient2Uuid,
            isDeleted = false
        )
    )

    val prescribedDrugsForPatient3 = listOf(
        TestData.prescription(
            uuid = UUID.fromString("7766017c-719e-4e12-b64a-3870f7a3b81b"),
            name = "Captopril",
            dosage = "10 mg",
            isDeleted = false,
            patientUuid = patient3Uuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("dcbf71ce-115d-4d5d-b4fd-10a292b92573"),
            name = "Temisartan",
            dosage = "15 mg",
            isDeleted = false,
            patientUuid = patient3Uuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("41468679-44b0-4170-a282-55431b33f8d8"),
            name = "Aspirin",
            dosage = "15 mg",
            isDeleted = true,
            patientUuid = patient3Uuid
        )
    )

    patientRepository.save(listOf(patient1, patient2, patient3))
    appointmentRepository.save(listOf(appointmentForPatient1, appointmentForPatient2, appointmentForPatient3))
    prescriptionRepository.save(prescribedDrugsForPatient1 + prescribedDrugsForPatient2 + prescribedDrugsForPatient3)

    // when
    val generatedCsv = csvGenerator.generate(listOf(appointmentForPatient1UUID, appointmentForPatient3UUID)).toString()

    // then
    val expectedCsv = """
      Overdue list downloaded at: 01 Jan 2018 12:00 AM
      Registration date,BP Passport number,Patient name,Gender,Age,Patient address,Patient village or colony,Days overdue,Patient phone,Latest medicines
      01-Feb-2017,773 3918,Ramesh Murthy,Male,47,Bhamini Burg,68691 Sarvin Drive,31,1111111111,"Amlodipine 25 mg, Atenolol 25 mg"
      01-Jan-2017,082 0547,Shreya Mishra,Female,31,Enakshi Burg,18473 Vaishnavi Curve,25,2222222222,"Captopril 10 mg, Temisartan 15 mg"

    """.trimIndent()
    assertThat(generatedCsv).isEqualTo(expectedCsv)
  }
}
