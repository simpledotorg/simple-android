package org.simple.clinic.rules

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.sync.PatientPushRequest
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.clinic.user.UserSession
import java.util.UUID
import javax.inject.Inject

class RegisterPatientRule(val patientUuid: UUID) : TestRule {

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var patientSyncApi: PatientSyncApi

  private fun registerPatient() {
    val currentFacilityId = facilityRepository.currentFacilityUuid()!!

    val patientPayload = testData.patientPayload(
        uuid = patientUuid,
        registeredFacilityId = currentFacilityId,
        assignedFacilityId = currentFacilityId
    )
    val patientPushRequest = PatientPushRequest(listOf(patientPayload))

    patientSyncApi
        .push(patientPushRequest)
        .execute()
  }

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        TestClinicApp.appComponent().inject(this@RegisterPatientRule)
        registerPatient()
        base.evaluate()
      }
    }
  }
}
