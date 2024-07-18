package org.simple.clinic.drugstockreminders.enterdrugstock

sealed class EnterDrugStockEffect

data object LoadDrugStockFormUrl : EnterDrugStockEffect()
