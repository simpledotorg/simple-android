package org.simple.clinic.drugs

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.drugs.EditMedicineButtonState.REFILL_MEDICINE
import org.simple.clinic.drugs.EditMedicineButtonState.SAVE_MEDICINE
import org.simple.clinic.drugs.selection.CustomPrescribedDrugListItem
import org.simple.clinic.drugs.selection.EditMedicinesUi
import org.simple.clinic.drugs.selection.ProtocolDrugListItem
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.BD
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.OD
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.QDS
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.TDS
import java.time.Instant
import java.util.UUID

class EditMedicinesUiRendererTest {

  private val ui = mock<EditMedicinesUi>()
  private val uiRenderer = EditMedicinesUiRenderer(ui)

  private val patientUuid = UUID.fromString("00f6ad74-703a-4176-acaa-fc6b57b4fa3c")
  private val defaultModel = EditMedicinesModel.create(patientUuid)
  private val medicineFrequencyToLabelMap = mapOf(
      null to DrugFrequencyLabel(label = "None"),
      OD to DrugFrequencyLabel(label = "OD"),
      BD to DrugFrequencyLabel(label = "BD"),
      TDS to DrugFrequencyLabel(label = "TDS"),
      QDS to DrugFrequencyLabel(label = "QDS")
  )

  @Test
  fun `when prescribed drug is no longer present in protocol, it should be rendered as custom drug`() {
    // given
    val amlodipine5mg = TestData.protocolDrug(name = "Amlodipine", dosage = "5mg")
    val amlodipine10mg = TestData.protocolDrug(name = "Amlodipine", dosage = "10mg")

    val protocolDrugAndDosages = listOf(
        ProtocolDrugAndDosages(amlodipine10mg.name, listOf(amlodipine5mg, amlodipine10mg))
    )

    val amlodipine10mgPrescription = TestData.prescription(
        uuid = UUID.fromString("90e28866-90f6-48a0-add1-cf44aa43209c"),
        name = "Amlodipine",
        dosage = "10mg",
        isProtocolDrug = true,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val telmisartan40mgPrescription = TestData.prescription(
        uuid = UUID.fromString("ac3cfff0-2ebf-4c9c-adab-a41cc8a0bbeb"),
        name = "Telmisartan",
        dosage = "40mg",
        isProtocolDrug = true,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val fooPrescription = TestData.prescription(
        uuid = UUID.fromString("68dc8060-bed4-4e1b-9891-7d77cad9639e"),
        name = "Foo",
        dosage = "2 pills",
        isProtocolDrug = false,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val barPrescription = TestData.prescription(
        uuid = UUID.fromString("b5eb5dfa-f131-4d9f-a2d2-41d56aa109da"),
        name = "Bar",
        dosage = null,
        isProtocolDrug = false,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val prescriptions = listOf(
        amlodipine10mgPrescription,
        telmisartan40mgPrescription,
        fooPrescription,
        barPrescription)

    val model = defaultModel
        .protocolDrugsFetched(protocolDrugAndDosages)
        .prescribedDrugsFetched(prescriptions)
        .editMedicineDrugStateFetched(SAVE_MEDICINE)
        .medicineFrequencyToLabelMapLoaded(medicineFrequencyToLabelMap)

    // when
    uiRenderer.render(model)

    // then
    val drugsList = listOf(
        ProtocolDrugListItem(
            id = 0,
            drugName = amlodipine10mg.name,
            prescribedDrug = amlodipine10mgPrescription,
            hasTopCorners = true,
            medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = telmisartan40mgPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = fooPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = barPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap))

    verify(ui).populateDrugsList(drugsList)
    verify(ui).showDoneButton()
    verify(ui).hideRefillMedicineButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when edit medicine button state is to save medicine, then show done button and hide refill medicine button`() {
    // given
    val amlodipine5mg = TestData.protocolDrug(name = "Amlodipine", dosage = "5mg")
    val amlodipine10mg = TestData.protocolDrug(name = "Amlodipine", dosage = "10mg")

    val protocolDrugAndDosages = listOf(
        ProtocolDrugAndDosages(amlodipine10mg.name, listOf(amlodipine5mg, amlodipine10mg))
    )

    val amlodipine10mgPrescription = TestData.prescription(
        uuid = UUID.fromString("90e28866-90f6-48a0-add1-cf44aa43209c"),
        name = "Amlodipine",
        dosage = "10mg",
        isProtocolDrug = true,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val telmisartan40mgPrescription = TestData.prescription(
        uuid = UUID.fromString("ac3cfff0-2ebf-4c9c-adab-a41cc8a0bbeb"),
        name = "Telmisartan",
        dosage = "40mg",
        isProtocolDrug = true,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val fooPrescription = TestData.prescription(
        uuid = UUID.fromString("68dc8060-bed4-4e1b-9891-7d77cad9639e"),
        name = "Foo",
        dosage = "2 pills",
        isProtocolDrug = false,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val barPrescription = TestData.prescription(
        uuid = UUID.fromString("b5eb5dfa-f131-4d9f-a2d2-41d56aa109da"),
        name = "Bar",
        dosage = null,
        isProtocolDrug = false,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val prescriptions = listOf(
        amlodipine10mgPrescription,
        telmisartan40mgPrescription,
        fooPrescription,
        barPrescription)

    val prescribedDrugsFetchedModel = defaultModel
        .protocolDrugsFetched(protocolDrugAndDosages)
        .prescribedDrugsFetched(prescriptions)
        .editMedicineDrugStateFetched(SAVE_MEDICINE)
        .medicineFrequencyToLabelMapLoaded(medicineFrequencyToLabelMap)

    // when
    uiRenderer.render(prescribedDrugsFetchedModel)

    // then
    val drugsList = listOf(
        ProtocolDrugListItem(
            id = 0,
            drugName = amlodipine10mg.name,
            prescribedDrug = amlodipine10mgPrescription,
            hasTopCorners = true,
            medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = telmisartan40mgPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = fooPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = barPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap))

    verify(ui).populateDrugsList(drugsList)
    verify(ui).showDoneButton()
    verify(ui).hideRefillMedicineButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when edit medicine button state is refill medicine, then show refill medicine button `() {
    // given
    val amlodipine5mg = TestData.protocolDrug(name = "Amlodipine", dosage = "5mg")
    val amlodipine10mg = TestData.protocolDrug(name = "Amlodipine", dosage = "10mg")

    val protocolDrugAndDosages = listOf(
        ProtocolDrugAndDosages(amlodipine10mg.name, listOf(amlodipine5mg, amlodipine10mg))
    )

    val amlodipine10mgPrescription = TestData.prescription(
        uuid = UUID.fromString("90e28866-90f6-48a0-add1-cf44aa43209c"),
        name = "Amlodipine",
        dosage = "10mg",
        isProtocolDrug = true,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val telmisartan40mgPrescription = TestData.prescription(
        uuid = UUID.fromString("ac3cfff0-2ebf-4c9c-adab-a41cc8a0bbeb"),
        name = "Telmisartan",
        dosage = "40mg",
        isProtocolDrug = true,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val fooPrescription = TestData.prescription(
        uuid = UUID.fromString("68dc8060-bed4-4e1b-9891-7d77cad9639e"),
        name = "Foo",
        dosage = "2 pills",
        isProtocolDrug = false,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val barPrescription = TestData.prescription(
        uuid = UUID.fromString("b5eb5dfa-f131-4d9f-a2d2-41d56aa109da"),
        name = "Bar",
        dosage = null,
        isProtocolDrug = false,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val prescriptions = listOf(
        amlodipine10mgPrescription,
        telmisartan40mgPrescription,
        fooPrescription,
        barPrescription)

    val prescribedDrugsFetchedModel = defaultModel
        .protocolDrugsFetched(protocolDrugAndDosages)
        .prescribedDrugsFetched(prescriptions)
        .editMedicineDrugStateFetched(REFILL_MEDICINE)
        .medicineFrequencyToLabelMapLoaded(medicineFrequencyToLabelMap)

    // when
    uiRenderer.render(prescribedDrugsFetchedModel)

    // then
    val drugsList = listOf(
        ProtocolDrugListItem(
            id = 0,
            drugName = amlodipine10mg.name,
            prescribedDrug = amlodipine10mgPrescription,
            hasTopCorners = true,
            medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = telmisartan40mgPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = fooPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = barPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap))

    verify(ui).populateDrugsList(drugsList)
    verify(ui).showRefillMedicineButton()
    verify(ui).hideDoneButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when custom drug is top of the list, then apply corner radius`() {
    // given
    val amlodipine5mg = TestData.protocolDrug(name = "Amlodipine", dosage = "5mg")
    val amlodipine10mg = TestData.protocolDrug(name = "Amlodipine", dosage = "10mg")

    val protocolDrugAndDosages = listOf(
        ProtocolDrugAndDosages(amlodipine10mg.name, listOf(amlodipine5mg, amlodipine10mg))
    )

    val amlodipine10mgPrescription = TestData.prescription(
        uuid = UUID.fromString("90e28866-90f6-48a0-add1-cf44aa43209c"),
        name = "Amlodipine",
        dosage = "10mg",
        isProtocolDrug = true,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val telmisartan40mgPrescription = TestData.prescription(
        uuid = UUID.fromString("ac3cfff0-2ebf-4c9c-adab-a41cc8a0bbeb"),
        name = "Telmisartan",
        dosage = "40mg",
        isProtocolDrug = true,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val fooPrescription = TestData.prescription(
        uuid = UUID.fromString("68dc8060-bed4-4e1b-9891-7d77cad9639e"),
        name = "Foo",
        dosage = "2 pills",
        isProtocolDrug = false,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val barPrescription = TestData.prescription(
        uuid = UUID.fromString("b5eb5dfa-f131-4d9f-a2d2-41d56aa109da"),
        name = "Bar",
        dosage = null,
        isProtocolDrug = false,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val prescriptions = listOf(
        telmisartan40mgPrescription,
        fooPrescription,
        barPrescription)

    val prescribedDrugsFetchedModel = defaultModel
        .protocolDrugsFetched(protocolDrugAndDosages)
        .prescribedDrugsFetched(prescriptions)
        .editMedicineDrugStateFetched(REFILL_MEDICINE)
        .medicineFrequencyToLabelMapLoaded(medicineFrequencyToLabelMap)

    // when
    uiRenderer.render(prescribedDrugsFetchedModel)

    // then
    val drugsList = listOf(
        CustomPrescribedDrugListItem(prescribedDrug = telmisartan40mgPrescription, hasTopCorners = true, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = fooPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        CustomPrescribedDrugListItem(prescribedDrug = barPrescription, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap),
        ProtocolDrugListItem(id = 0, drugName = amlodipine10mg.name, prescribedDrug = null, hasTopCorners = false, medicineFrequencyToLabelMap = medicineFrequencyToLabelMap))

    verify(ui).populateDrugsList(drugsList)
    verify(ui).showRefillMedicineButton()
    verify(ui).hideDoneButton()
    verifyNoMoreInteractions(ui)
  }
}
