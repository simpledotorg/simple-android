package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns
import org.simple.clinic.assertValues
import org.simple.clinic.insert

class Migration120AndroidTest : BaseDatabaseMigrationTest(119, 120) {

  @Test
  fun cholesterol_value_should_be_added_to_medical_history_table() {
    before.insert("MedicalHistory", mapOf(
        "uuid" to "fe905238-411a-48c9-90df-c139e5d41214",
        "patientUuid" to "feba322d-5019-47a6-992a-3faa288a0d41",
        "diagnosedWithHypertension" to "no",
        "isOnHypertensionTreatment" to "no",
        "isOnDiabetesTreatment" to "no",
        "hasHadHeartAttack" to "no",
        "hasHadStroke" to "no",
        "hasHadKidneyDisease" to "no",
        "hasDiabetes" to "no",
        "isSmoking" to "no",
        "syncStatus" to "no",
        "createdAt" to "no",
        "updatedAt" to "no",
        "deletedAt" to "no"
    ))
    before.assertColumns("MedicalHistory", setOf(
        "uuid",
        "patientUuid",
        "diagnosedWithHypertension",
        "isOnHypertensionTreatment",
        "isOnDiabetesTreatment",
        "hasHadHeartAttack",
        "hasHadStroke",
        "hasHadKidneyDisease",
        "hasDiabetes",
        "isSmoking",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))

    after.assertColumns("MedicalHistory", setOf(
        "uuid",
        "patientUuid",
        "diagnosedWithHypertension",
        "isOnHypertensionTreatment",
        "isOnDiabetesTreatment",
        "hasHadHeartAttack",
        "hasHadStroke",
        "hasHadKidneyDisease",
        "hasDiabetes",
        "isSmoking",
        "cholesterol_value",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))

    val cursor = after.query("SELECT cholesterol_value FROM MedicalHistory WHERE uuid = 'fe905238-411a-48c9-90df-c139e5d41214'")
    cursor.moveToFirst()
    cursor.assertValues(mapOf(
        "cholesterol_value" to null
    ))
  }
}
