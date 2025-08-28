package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration121AndroidTest : BaseDatabaseMigrationTest(120, 121) {

  @Test
  fun isSmoking_should_be_added_to_medical_history_table() {
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
        "cholesterol_value",
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
        "isUsingSmokelessTobacco",
        "cholesterol_value",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))
  }
}
