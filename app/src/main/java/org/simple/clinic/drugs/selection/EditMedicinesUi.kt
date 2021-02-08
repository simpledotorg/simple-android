package org.simple.clinic.drugs.selection

import androidx.viewbinding.ViewBinding
import org.simple.clinic.summary.GroupieItemWithUiEvents

interface EditMedicinesUi {
  fun populateDrugsList(protocolDrugItems: List<GroupieItemWithUiEvents<out ViewBinding>>)
  fun showDoneButton()
  fun hideRefillMedicineButton()
  fun showRefillMedicineButton()
  fun hideDoneButton()
}
