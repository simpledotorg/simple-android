package org.simple.clinic.drugs.selection

interface EditMedicinesUi {
  fun populateDrugsList(protocolDrugItems: List<DrugListItem>)
  fun showDoneButton()
  fun hideRefillMedicineButton()
  fun showRefillMedicineButton()
  fun hideDoneButton()
}
