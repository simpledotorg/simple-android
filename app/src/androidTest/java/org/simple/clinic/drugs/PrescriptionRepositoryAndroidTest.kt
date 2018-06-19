package org.simple.clinic.drugs

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.protocol.ProtocolDrug
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class PrescriptionRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var repository: PrescriptionRepository

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun prescriptions_for_a_patient_should_exclude_soft_deleted_prescriptions() {
    val patientUuid = UUID.randomUUID()
    val protocolUuid = UUID.randomUUID()
    val amlodipine = ProtocolDrug(UUID.randomUUID(), "Amlodipine", rxNormCode = null, dosages = listOf("5mg", "10mg"), protocolUUID = protocolUuid)

    repository.savePrescription(patientUuid, amlodipine, "5mg").blockingAwait()

    val savedPrescriptions1 = repository.newestPrescriptionsForPatient(patientUuid).blockingFirst()
    assertThat(savedPrescriptions1).hasSize(1)

    repository.savePrescription(patientUuid, amlodipine, "10mg")
        .andThen(repository.softDeletePrescription(savedPrescriptions1[0].uuid))
        .blockingAwait()

    val savedPrescriptions2 = repository.newestPrescriptionsForPatient(patientUuid).blockingFirst()
    assertThat(savedPrescriptions2).hasSize(1)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }
}
