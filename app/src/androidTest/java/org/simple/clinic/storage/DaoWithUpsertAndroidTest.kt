package org.simple.clinic.storage

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.Gender.FEMALE
import org.simple.clinic.patient.Gender.MALE
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import java.util.UUID
import javax.inject.Inject

class DaoWithUpsertAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var testData: TestData

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }

  @Test
  fun when_upserting_a_record_it_should_be_inserted_if_it_doesnt_exist_already() {
    val patientUuid = UUID.randomUUID()
    val address = testData.patientAddress(patientUuid)
    val patient = testData.patient(uuid = patientUuid, addressUuid = address.uuid)

    database.addressDao().save(address)
    database.patientDao().save(patient)

    val storedPatient = database.patientDao().patient(patientUuid).blockingFirst().first()
    assertThat(storedPatient.uuid).isEqualTo(patientUuid)
  }

  @Test
  fun when_upserting_a_record_it_should_be_updated_if_it_already_exists() {
    val patientUuid = UUID.randomUUID()
    val address = testData.patientAddress(patientUuid)
    val patient = testData.patient(uuid = patientUuid, addressUuid = address.uuid, gender = MALE)
    val patientNumber = testData.patientPhoneNumber(patientUuid = patientUuid, number = "123")

    database.addressDao().save(address)
    database.patientDao().save(patient)
    database.phoneNumberDao().save(listOf(patientNumber))

    val updatedPatient = patient.copy(gender = FEMALE)
    database.patientDao().save(updatedPatient)

    val storedPatient = database.patientDao().patient(patientUuid).blockingFirst().first()
    assertThat(storedPatient.uuid).isEqualTo(patientUuid)
    assertThat(storedPatient.gender).isEqualTo(FEMALE)

    val storedNumbers = database.phoneNumberDao().count()
    assertThat(storedNumbers).isEqualTo(1)
  }

  @Test
  fun upserting_multiple_records_should_work_correctly() {
    val addresses = mutableListOf<PatientAddress>()
    val patients = mutableListOf<Patient>()

    val generatePatientAndAddress = {
      val patientUuid = UUID.randomUUID()
      val address = testData.patientAddress(patientUuid)
      val patient = testData.patient(uuid = patientUuid, addressUuid = address.uuid)
      patient to address
    }

    (0 until 10).forEach {
      val (patient, address) = generatePatientAndAddress()
      addresses += address
      patients += patient
    }

    database.addressDao().save(addresses)
    database.patientDao().save(patients)

    val newAndUpdatedAddresses = mutableListOf<PatientAddress>()
    newAndUpdatedAddresses.addAll(addresses)

    val newAndUpdatedPatients = mutableListOf<Patient>()
    newAndUpdatedPatients.addAll(patients)

    (0 until 10).forEach {
      val (patient, address) = generatePatientAndAddress()
      newAndUpdatedAddresses += address
      newAndUpdatedPatients += patient
    }

    database.addressDao().save(newAndUpdatedAddresses)
    database.patientDao().save(newAndUpdatedPatients)

    val storedAddressesCount = database.addressDao().count()
    assertThat(storedAddressesCount).isEqualTo(20)

    val storedPatientsCount = database.patientDao().patientCount().blockingFirst()
    assertThat(storedPatientsCount).isEqualTo(20)
  }
}
