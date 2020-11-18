package org.simple.clinic.drugs

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.EditMedicineButtonState.REFILL_MEDICINE
import org.simple.clinic.drugs.EditMedicineButtonState.SAVE_MEDICINE
import org.simple.clinic.drugs.selection.EditMedicinesUi
import org.simple.clinic.drugs.selection.ProtocolDrugListItem
import org.simple.clinic.drugs.selection.entry.CustomPrescribedDrugListItem
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import java.util.UUID

class EditMedicinesUiRendererTest {

  private val ui = mock<EditMedicinesUi>()
  private val uiRenderer = EditMedicinesUiRenderer(ui)

  private val patientUuid = UUID.fromString("00f6ad74-703a-4176-acaa-fc6b57b4fa3c")
  private val defaultModel = EditMedicinesModel.create(patientUuid)

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
        isProtocolDrug = true
    )
    val telmisartan40mgPrescription = TestData.prescription(
        uuid = UUID.fromString("ac3cfff0-2ebf-4c9c-adab-a41cc8a0bbeb"),
        name = "Telmisartan",
        dosage = "40mg",
        isProtocolDrug = true
    )
    val fooPrescription = TestData.prescription(
        uuid = UUID.fromString("68dc8060-bed4-4e1b-9891-7d77cad9639e"),
        name = "Foo",
        dosage = "2 pills",
        isProtocolDrug = false
    )
    val barPrescription = TestData.prescription(
        uuid = UUID.fromString("b5eb5dfa-f131-4d9f-a2d2-41d56aa109da"),
        name = "Bar",
        dosage = null,
        isProtocolDrug = false
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

    // when
    uiRenderer.render(model)

    // then
    val drugsList = listOf(
        ProtocolDrugListItem(
            id = 0,
            drugName = amlodipine10mg.name,
            prescribedDrug = amlodipine10mgPrescription,
            hideDivider = false),
        CustomPrescribedDrugListItem(telmisartan40mgPrescription, false),
        CustomPrescribedDrugListItem(fooPrescription, false),
        CustomPrescribedDrugListItem(barPrescription, true))

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
        isProtocolDrug = true
    )
    val telmisartan40mgPrescription = TestData.prescription(
        uuid = UUID.fromString("ac3cfff0-2ebf-4c9c-adab-a41cc8a0bbeb"),
        name = "Telmisartan",
        dosage = "40mg",
        isProtocolDrug = true
    )
    val fooPrescription = TestData.prescription(
        uuid = UUID.fromString("68dc8060-bed4-4e1b-9891-7d77cad9639e"),
        name = "Foo",
        dosage = "2 pills",
        isProtocolDrug = false
    )
    val barPrescription = TestData.prescription(
        uuid = UUID.fromString("b5eb5dfa-f131-4d9f-a2d2-41d56aa109da"),
        name = "Bar",
        dosage = null,
        isProtocolDrug = false
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

    // when
    uiRenderer.render(prescribedDrugsFetchedModel)

    // then
    val drugsList = listOf(
        ProtocolDrugListItem(
            id = 0,
            drugName = amlodipine10mg.name,
            prescribedDrug = amlodipine10mgPrescription,
            hideDivider = false),
        CustomPrescribedDrugListItem(telmisartan40mgPrescription, false),
        CustomPrescribedDrugListItem(fooPrescription, false),
        CustomPrescribedDrugListItem(barPrescription, true))

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
        isProtocolDrug = true
    )
    val telmisartan40mgPrescription = TestData.prescription(
        uuid = UUID.fromString("ac3cfff0-2ebf-4c9c-adab-a41cc8a0bbeb"),
        name = "Telmisartan",
        dosage = "40mg",
        isProtocolDrug = true
    )
    val fooPrescription = TestData.prescription(
        uuid = UUID.fromString("68dc8060-bed4-4e1b-9891-7d77cad9639e"),
        name = "Foo",
        dosage = "2 pills",
        isProtocolDrug = false
    )
    val barPrescription = TestData.prescription(
        uuid = UUID.fromString("b5eb5dfa-f131-4d9f-a2d2-41d56aa109da"),
        name = "Bar",
        dosage = null,
        isProtocolDrug = false
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

    // when
    uiRenderer.render(prescribedDrugsFetchedModel)

    // then
    val drugsList = listOf(
        ProtocolDrugListItem(
            id = 0,
            drugName = amlodipine10mg.name,
            prescribedDrug = amlodipine10mgPrescription,
            hideDivider = false),
        CustomPrescribedDrugListItem(telmisartan40mgPrescription, false),
        CustomPrescribedDrugListItem(fooPrescription, false),
        CustomPrescribedDrugListItem(barPrescription, true))

    verify(ui).populateDrugsList(drugsList)
    verify(ui).showRefillMedicineButton()
    verify(ui).hideDoneButton()
    verifyNoMoreInteractions(ui)
  }
}
