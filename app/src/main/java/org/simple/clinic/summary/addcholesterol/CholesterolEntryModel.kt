package org.simple.clinic.summary.addcholesterol

data class CholesterolEntryModel(
    val cholesterolValue: Float
) {

  companion object {
    fun create(): CholesterolEntryModel {
      return CholesterolEntryModel(
          cholesterolValue = 0f
      )
    }
  }

  fun cholesterolChanged(cholesterolValue: Float): CholesterolEntryModel {
    return copy(cholesterolValue = cholesterolValue)
  }
}
