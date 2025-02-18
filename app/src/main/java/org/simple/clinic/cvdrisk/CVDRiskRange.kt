package org.simple.clinic.cvdrisk

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize

@Parcelize
data class CVDRiskRange(
    val min: Int,
    val max: Int,
) : Parcelable {

  val level: CVDRiskLevel
    get() = CVDRiskLevel.compute(this)

  val canPrescribeStatin: Boolean
    get() = max >= 10

  class RoomTypeConverter {

    @TypeConverter
    fun toRiskRange(risk: String) = parseRiskRange(risk)

    @TypeConverter
    fun fromRiskRange(riskRange: CVDRiskRange) = formatRiskRange(riskRange)
  }

  class MoshiTypeAdapter {

    @ToJson
    fun toJson(riskRange: CVDRiskRange) = formatRiskRange(riskRange)

    @FromJson
    fun fromJson(risk: String) = parseRiskRange(risk)
  }

  companion object {

    fun from(risks: List<Int>): CVDRiskRange? {
      return when {
        risks.isEmpty() -> null
        risks.size == 1 -> CVDRiskRange(min = risks.first(), max = risks.first())
        else -> CVDRiskRange(risks.min(), risks.max())
      }
    }

    fun parseRiskRange(risk: String): CVDRiskRange {
      val risks = risk.split("-").map { it.trim() }
      return when (risks.size) {
        2 -> CVDRiskRange(risks[0].toInt(), risks[1].toInt())
        1 -> {
          val value = risks[0].toInt()
          CVDRiskRange(value, value)
        }

        else -> throw IllegalArgumentException("Invalid risk range format: $risk")
      }
    }

    fun formatRiskRange(riskRange: CVDRiskRange): String {
      return if (riskRange.min == riskRange.max) {
        "${riskRange.min}"
      } else {
        "${riskRange.min} - ${riskRange.max}"
      }
    }
  }
}
