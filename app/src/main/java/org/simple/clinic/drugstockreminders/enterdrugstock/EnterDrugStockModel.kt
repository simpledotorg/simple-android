package org.simple.clinic.drugstockreminders.enterdrugstock

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnterDrugStockModel(
    val drugStockFormUrl: String?
) : Parcelable {

  companion object {

    fun create() = EnterDrugStockModel(
        drugStockFormUrl = null
    )
  }
}
