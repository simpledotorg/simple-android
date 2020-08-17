package org.simple.clinic.teleconsultlog.drugduration

data class DrugDurationModel(
    val duration: String
) {

  companion object {
    fun create(duration: String) = DrugDurationModel(duration)
  }

  val hasDuration: Boolean
    get() = duration.isNotBlank()

  fun durationChanged(duration: String): DrugDurationModel {
    return copy(duration = duration)
  }
}
