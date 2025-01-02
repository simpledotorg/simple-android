package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration116AndroidTest : BaseDatabaseMigrationTest(115, 116) {

  @Test
  fun smoking_should_be_added_to_medical_history_table() {
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
        "smoking",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))
  }
}
