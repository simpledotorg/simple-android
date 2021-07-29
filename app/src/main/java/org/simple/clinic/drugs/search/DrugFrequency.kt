package org.simple.clinic.drugs.search

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class DrugFrequency : Parcelable {

  @Parcelize
  object OD : DrugFrequency() {
    override fun toString() = "OD"
  }

  @Parcelize
  object BD : DrugFrequency() {
    override fun toString() = "BD"
  }

  @Parcelize
  object TDS : DrugFrequency() {
    override fun toString() = "TDS"
  }

  @Parcelize
  object QDS : DrugFrequency() {
    override fun toString() = "QDS"
  }

  @Parcelize
  data class Unknown(val actualValue: String) : DrugFrequency()

  object TypeAdapter : SafeEnumTypeAdapter<DrugFrequency>(
      knownMappings = mapOf(
          OD to "one_per_day",
          BD to "two_per_day",
          TDS to "three_per_day",
          QDS to "four_per_day"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {
    @TypeConverter
    fun toEnum(value: String?): DrugFrequency? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(DrugFrequency: DrugFrequency?): String? = TypeAdapter.fromEnum(DrugFrequency)
  }

  class MoshiTypeConverter {
    @FromJson
    fun toEnum(value: String?): DrugFrequency? = TypeAdapter.toEnum(value)

    @ToJson
    fun fromEnum(DrugFrequency: DrugFrequency?): String? = TypeAdapter.fromEnum(DrugFrequency)
  }


  companion object {
    fun fromMedicineFrequencyToDrugFrequency(value: MedicineFrequency?): DrugFrequency {
      return when (value) {
        MedicineFrequency.BD -> BD
        MedicineFrequency.OD -> OD
        MedicineFrequency.QDS -> QDS
        MedicineFrequency.TDS -> TDS
        else -> Unknown("None")
      }
    }
  }
}
