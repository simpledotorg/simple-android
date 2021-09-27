package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.time.Duration
import java.util.UUID

class TeleconsultMedicinesUpdateTest {
  private val patientUuid = UUID.fromString("134a6669-7b4c-42dd-a763-e3015b7933ff")
  private val model = TeleconsultMedicinesModel.create(patientUuid = patientUuid)

  private val updateSpec = UpdateSpec(TeleconsultMedicinesUpdate())

  @Test
  fun `when patient medicines are loaded, then update the model`() {
    val medicines = listOf(
        TestData.prescription(
            uuid = UUID.fromString("9fde23c1-d932-4bef-877c-88ade55e359a"),
            patientUuid = patientUuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("41753f87-3823-40b5-972f-9533539d8324"),
            patientUuid = patientUuid
        )
    )

    updateSpec
        .given(model)
        .whenEvent(PatientMedicinesLoaded(medicines))
        .then(assertThatNext(
            hasModel(model.medicinesLoaded(medicines)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when drug duration is clicked, then open drug duration sheet`() {
    val prescription = TestData.prescription(
        uuid = UUID.fromString("b9c52365-2782-4c03-95ac-1c508a11a510")
    )

    updateSpec
        .given(model)
        .whenEvent(DrugDurationClicked(prescription))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenDrugDurationSheet(prescription))
        ))
  }

  @Test
  fun `when drug frequency is clicked, then open drug frequency sheet`() {
    val prescription = TestData.prescription(
        uuid = UUID.fromString("b9c52365-2782-4c03-95ac-1c508a11a510")
    )

    updateSpec
        .given(model)
        .whenEvent(DrugFrequencyClicked(prescription))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenDrugFrequencySheet(prescription))
        ))
  }

  @Test
  fun `when drug duration is changed, then update drug duration`() {
    val prescriptionUuid = UUID.fromString("320ca8fe-88b7-44ea-b1b6-d35cfaa27730")
    val duration = Duration.ofDays(25)

    updateSpec
        .given(model)
        .whenEvent(DrugDurationChanged(prescriptionUuid, duration))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(UpdateDrugDuration(prescriptionUuid, duration))
        ))
  }

  @Test
  fun `when drug frequency is changed, then update drug frequency`() {
    val prescriptionUuid = UUID.fromString("320ca8fe-88b7-44ea-b1b6-d35cfaa27730")
    val frequency = MedicineFrequency.TDS

    updateSpec
        .given(model)
        .whenEvent(DrugFrequencyChanged(prescriptionUuid, frequency))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(UpdateDrugFrequency(prescriptionUuid, frequency))
        ))
  }

  @Test
  fun `when edit medicine is clicked and there are medicines, then open edit medicine screen with refill medicines open intention`() {
    val prescribedDrugRecords = listOf(
        TestData.prescription(uuid = UUID.fromString("4aec376e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine1"),
        TestData.prescription(uuid = UUID.fromString("537a119e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine2"),
        TestData.prescription(uuid = UUID.fromString("5ac2a678-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine3"),
        TestData.prescription(uuid = UUID.fromString("5f9f0fe2-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine4"),
    )

    updateSpec
        .given(model.medicinesLoaded(prescribedDrugRecords))
        .whenEvent(EditMedicinesClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenEditMedicines(patientUuid)),
        ))
  }

  @Test
  fun `when edit medicine is clicked and there are no medicines, then open edit medicine screen with add new medicines open intention`() {
    val prescribedDrugRecords = emptyList<PrescribedDrug>()
    updateSpec
        .given(model.medicinesLoaded(prescribedDrugRecords))
        .whenEvent(EditMedicinesClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenEditMedicines(patientUuid)),
        ))
  }
}
