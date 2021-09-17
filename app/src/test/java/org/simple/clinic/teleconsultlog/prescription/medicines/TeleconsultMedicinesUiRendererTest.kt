package org.simple.clinic.teleconsultlog.prescription.medicines

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.util.UUID

class TeleconsultMedicinesUiRendererTest {

  private val patientUuid = UUID.fromString("e55ee888-c13f-4de1-b37f-bb3e05fce284")
  private val model = TeleconsultMedicinesModel.create(patientUuid)

  private val ui = mock<TeleconsultMedicinesUi>()
  private val uiRenderer = TeleconsultMedicinesUiRenderer(ui)

  @Test
  fun `when medicines are loading, then do nothing`() {
    // when
    uiRenderer.render(model)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when medicines are loaded, then render the medicines`() {
    // given
    val medicines = listOf(
        TestData.prescription(
            uuid = UUID.fromString("8ba9a9e4-7d60-4e69-ae77-1a81d89e520a")
        ),
        TestData.prescription(
            uuid = UUID.fromString("c3e45f9b-7023-4671-bfc5-4159fefe8aa2")
        )
    )
    val medicineFrequencyToFrequencyChoiceItemMap = mapOf(
        null to DrugFrequencyChoiceItem(drugFrequency = null, label = "None"),
        MedicineFrequency.OD to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.OD, label = "OD"),
        MedicineFrequency.BD to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.BD, label = "BD"),
        MedicineFrequency.TDS to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.TDS, label = "TDS"),
        MedicineFrequency.QDS to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.QDS, label = "QDS")
    )
    val medicinesLoadedModel = model
        .medicinesLoaded(medicines)
        .medicineFrequencyToFrequencyChoiceItemMapLoaded(medicineFrequencyToFrequencyChoiceItemMap)

    // when
    uiRenderer.render(medicinesLoadedModel)

    // then
    verify(ui).renderMedicines(medicines, medicineFrequencyToFrequencyChoiceItemMap)
    verify(ui).showEditButton()
    verify(ui).hideMedicinesRequiredError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when there are no medicines for the patient, then show no medicines`() {
    // given
    val noMedicinesModel = model.medicinesLoaded(emptyList())

    // when
    uiRenderer.render(noMedicinesModel)

    // then
    verify(ui).showNoMedicines()
    verify(ui).showAddButton()
    verifyNoMoreInteractions(ui)
  }
}
