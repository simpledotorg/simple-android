package org.simple.clinic.drugs

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.search.DrugFrequency.BD
import org.simple.clinic.drugs.search.DrugFrequency.OD
import org.simple.clinic.drugs.search.DrugFrequency.QDS
import org.simple.clinic.drugs.search.DrugFrequency.TDS
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class EditMedicineUpdateTest {

  private val patientUuid = UUID.fromString("871a2f40-2bda-488c-9443-7dc708c3743a")

  @Test
  fun `when prescribed drugs refill done is clicked, then refill medicines`() {
    val updateSpec = UpdateSpec(EditMedicinesUpdate(LocalDate.of(2020, 11, 18), ZoneOffset.UTC))
    val model = EditMedicinesModel.create(patientUuid)
    val prescribedDrugRecords = listOf(
        TestData.prescription(uuid = UUID.fromString("4aec376e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine1"),
        TestData.prescription(uuid = UUID.fromString("537a119e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine2"),
        TestData.prescription(uuid = UUID.fromString("5ac2a678-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine3"),
        TestData.prescription(uuid = UUID.fromString("5f9f0fe2-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine4"),
    )

    val prescribedDrugsFetchedModel = model.prescribedDrugsFetched(prescribedDrugRecords)
    updateSpec
        .given(prescribedDrugsFetchedModel)
        .whenEvent(PresribedDrugsRefillClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(RefillMedicines(patientUuid)),
        ))
  }

  @Test
  fun `when the prescription has been updated on the current date, then the save medicine button must be shown`() {
    val model = EditMedicinesModel.create(patientUuid)
    val updateSpec = UpdateSpec(EditMedicinesUpdate(LocalDate.parse("2018-01-01"), ZoneOffset.UTC))
    val prescribedDrugRecords = listOf(
        TestData.prescription(uuid = UUID.fromString("4aec376e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine1", createdAt = Instant.parse("2018-01-01T00:00:00Z"), updatedAt = Instant.parse("2018-01-01T00:00:00Z")),
        TestData.prescription(uuid = UUID.fromString("537a119e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine2", createdAt = Instant.parse("2018-01-01T00:00:00Z"), updatedAt = Instant.parse("2018-01-01T00:00:00Z")),
        TestData.prescription(uuid = UUID.fromString("5ac2a678-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine3", createdAt = Instant.parse("2018-01-01T00:00:00Z"), updatedAt = Instant.parse("2018-01-01T00:00:00Z")),
    )

    val protocolDrugRecords = listOf(
        TestData.protocolDrug(uuid = UUID.fromString("5f9f0fe2-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine4", createdAt = Instant.parse("2018-01-01T00:00:00Z"), updatedAt = Instant.parse("2018-01-01T00:00:00Z"))
    )

    val protocolDrugsAndDosages = listOf(ProtocolDrugAndDosages("Amlodipine5", protocolDrugRecords))

    val drugsFetchedAndSaveMedicineModel = model.prescribedDrugsFetched(prescribedDrugRecords).protocolDrugsFetched(protocolDrugsAndDosages).editMedicineDrugStateFetched(EditMedicineButtonState.SAVE_MEDICINE)
    updateSpec
        .given(model)
        .whenEvent(DrugsListFetched(protocolDrugsAndDosages, prescribedDrugRecords))
        .then(assertThatNext(
            hasModel(drugsFetchedAndSaveMedicineModel),
            hasNoEffects()
        ))
  }

  @Test
  fun `when prescription is empty and it has not been updated on the current day, then the save medicine button must be shown`() {
    val model = EditMedicinesModel.create(patientUuid)
    val updateSpec = UpdateSpec(EditMedicinesUpdate(LocalDate.of(2020, 11, 18), ZoneOffset.UTC))
    val prescribedDrugRecords = listOf<PrescribedDrug>()

    val protocolDrugRecords = emptyList<ProtocolDrug>()

    val protocolDrugsAndDosages = listOf(ProtocolDrugAndDosages("Amlodipine5", protocolDrugRecords))

    val drugsFetchedAndSaveMedicineModel = model.prescribedDrugsFetched(prescribedDrugRecords).protocolDrugsFetched(protocolDrugsAndDosages).editMedicineDrugStateFetched(EditMedicineButtonState.SAVE_MEDICINE)
    updateSpec
        .given(model)
        .whenEvent(DrugsListFetched(protocolDrugsAndDosages, prescribedDrugRecords))
        .then(assertThatNext(
            hasModel(drugsFetchedAndSaveMedicineModel),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the prescription has not been updated on the current day, then the refill medicine button must be shown`() {
    val model = EditMedicinesModel.create(patientUuid)
    val updateSpec = UpdateSpec(EditMedicinesUpdate(LocalDate.of(2020, 11, 18), ZoneOffset.UTC))
    val prescribedDrugRecords = listOf(
        TestData.prescription(uuid = UUID.fromString("4aec376e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine1", createdAt = Instant.parse("2012-12-12T00:00:00Z"), updatedAt = Instant.parse("2012-12-12T00:00:00Z")),
        TestData.prescription(uuid = UUID.fromString("537a119e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine2", createdAt = Instant.parse("2012-12-12T00:00:00Z"), updatedAt = Instant.parse("2012-12-12T00:00:00Z")),
        TestData.prescription(uuid = UUID.fromString("5ac2a678-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine3", createdAt = Instant.parse("2012-12-12T00:00:00Z"), updatedAt = Instant.parse("2012-12-12T00:00:00Z")),
    )

    val protocolDrugRecords = listOf(
        TestData.protocolDrug(uuid = UUID.fromString("5f9f0fe2-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine4", createdAt = Instant.parse("2012-12-12T00:00:00Z"), updatedAt = Instant.parse("2012-12-12T00:00:00Z"))
    )

    val protocolDrugsAndDosages = listOf(ProtocolDrugAndDosages("Amlodipine5", protocolDrugRecords))

    val drugsFetchedAndRefillMedicineModel = model.prescribedDrugsFetched(prescribedDrugRecords).protocolDrugsFetched(protocolDrugsAndDosages).editMedicineDrugStateFetched(EditMedicineButtonState.REFILL_MEDICINE)
    updateSpec
        .given(model)
        .whenEvent(DrugsListFetched(protocolDrugsAndDosages, prescribedDrugRecords))
        .then(assertThatNext(
            hasModel(drugsFetchedAndRefillMedicineModel),
            hasNoEffects()
        ))
  }

  @Test
  fun `when filled prescribed drugs are fetched and some of the drugs are deleted, then set edit medicine button state to save button`() {
    val model = EditMedicinesModel.create(patientUuid)
    val updateSpec = UpdateSpec(EditMedicinesUpdate(LocalDate.of(2020, 11, 18), ZoneOffset.UTC))
    val prescribedDrugRecords = listOf(
        TestData.prescription(uuid = UUID.fromString("4aec376e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine1", createdAt = Instant.parse("2020-11-18T00:00:00Z"), updatedAt = Instant.parse("2020-11-18T00:00:00Z")),
        TestData.prescription(uuid = UUID.fromString("537a119e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine2", createdAt = Instant.parse("2020-11-18T00:00:00Z"), updatedAt = Instant.parse("2020-11-18T00:00:00Z")),
        TestData.prescription(uuid = UUID.fromString("5ac2a678-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine3", createdAt = Instant.parse("2020-11-18T00:00:00Z"), updatedAt = Instant.parse("2020-11-18T00:00:00Z"), isDeleted = true),
    )

    val protocolDrugRecords = listOf(
        TestData.protocolDrug(uuid = UUID.fromString("5f9f0fe2-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine4", createdAt = Instant.parse("2020-11-18T00:00:00Z"), updatedAt = Instant.parse("2020-11-18T00:00:00Z"))
    )

    val protocolDrugsAndDosages = listOf(ProtocolDrugAndDosages("Amlodipine5", protocolDrugRecords))

    val drugsFetchedAndSaveMedicineModel = model.prescribedDrugsFetched(prescribedDrugRecords).protocolDrugsFetched(protocolDrugsAndDosages).editMedicineDrugStateFetched(EditMedicineButtonState.SAVE_MEDICINE)
    updateSpec
        .given(model)
        .whenEvent(DrugsListFetched(protocolDrugsAndDosages, prescribedDrugRecords))
        .then(assertThatNext(
            hasModel(drugsFetchedAndSaveMedicineModel),
            hasNoEffects()
        ))
  }

  @Test
  fun `when drug frequency choice items are loaded, then update the model with a map of medicine frequency to frequency choice items`() {
    val model = EditMedicinesModel.create(patientUuid)
    val updateSpec = UpdateSpec(EditMedicinesUpdate(LocalDate.of(2020, 11, 18), ZoneOffset.UTC))
    val drugFrequencyToLabelMap = mapOf(
        null to DrugFrequencyLabel(label = "None"),
        OD to DrugFrequencyLabel(label = "OD"),
        BD to DrugFrequencyLabel(label = "BD"),
        TDS to DrugFrequencyLabel(label = "TDS"),
        QDS to DrugFrequencyLabel(label = "QDS")
    )

    val medicineFrequencyToLabelMap = mapOf(
        null to DrugFrequencyLabel(label = "None"),
        MedicineFrequency.OD to DrugFrequencyLabel(label = "OD"),
        MedicineFrequency.BD to DrugFrequencyLabel(label = "BD"),
        MedicineFrequency.TDS to DrugFrequencyLabel(label = "TDS"),
        MedicineFrequency.QDS to DrugFrequencyLabel(label = "QDS")
    )

    updateSpec
        .given(model)
        .whenEvent(DrugFrequencyChoiceItemsLoaded(drugFrequencyToLabelMap))
        .then(assertThatNext(
            hasModel(model.medicineFrequencyToLabelMapLoaded(medicineFrequencyToLabelMap)),
            hasNoEffects()
        ))
  }
}
