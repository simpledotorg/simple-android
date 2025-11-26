package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration122AndroidTest : BaseDatabaseMigrationTest(121, 122) {

  @Test
  fun hypertension_diagnosed_at_and_diabetes_diagnosed_at_columns_should_be_added_to_medical_history_table() {
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
        "isUsingSmokelessTobacco",
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
        "hypertensionDiagnosedAt",
        "diabetesDiagnosedAt",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))
  }
}
