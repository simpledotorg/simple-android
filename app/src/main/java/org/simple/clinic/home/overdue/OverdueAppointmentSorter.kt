package org.simple.clinic.home.overdue

import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.returnscore.LikelyToReturnIfCalledScoreType
import org.simple.clinic.returnscore.ReturnScore
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max
import kotlin.random.Random

class OverdueAppointmentSorter @Inject constructor(
    private val returnScoreDao: ReturnScore.RoomDao,
    private val features: Features,
    private val random: Random = Random.Default
) {

  fun sort(overdueAppointments: List<OverdueAppointment>): List<SortedOverdueAppointment> {

    if (!features.isEnabled(Feature.SortOverdueBasedOnReturnScore)) {
      return overdueAppointments.map {
        SortedOverdueAppointment(
            appointment = it,
            score = 0f,
            bucket = OverdueBucket.REMAINING
        )
      }
    }

    val scores = returnScoreDao.getAllImmediate()
        .filter { it.scoreType == LikelyToReturnIfCalledScoreType }

    val scoreMap: Map<UUID, Float> = scores.associate {
      it.patientUuid to it.scoreValue
    }

    val withScores = overdueAppointments.map { appointment ->
      val score = scoreMap[appointment.appointment.patientUuid] ?: 0f
      appointment to score
    }

    val sorted = withScores.sortedByDescending { it.second }

    val total = sorted.size
    if (total == 0) return emptyList()

    val top20End = max((total * 0.2).toInt(), 1)
    val next30End = max((total * 0.5).toInt(), top20End)

    val top20 = sorted.take(top20End)
    val next30 = sorted.subList(top20End, next30End)
    val rest = sorted.drop(next30End)

    val topPickCount = max((top20.size * 0.5).toInt(), 1)
    val nextPickCount = max((next30.size * 0.5).toInt(), 1)

    val topPicked = top20.shuffled(random).take(topPickCount)
    val nextPicked = next30.shuffled(random).take(nextPickCount)

    val selectedAppointments = (topPicked + nextPicked)
        .map { it.first }
        .toSet()

    val topRemaining = top20.filterNot { it.first in selectedAppointments }
    val nextRemaining = next30.filterNot { it.first in selectedAppointments }

    fun mapToSorted(
        list: List<Pair<OverdueAppointment, Float>>,
        bucket: OverdueBucket
    ) = list.map { (appointment, score) ->
      SortedOverdueAppointment(
          appointment = appointment,
          score = score,
          bucket = bucket
      )
    }

    return buildList {
      addAll(mapToSorted(topPicked, OverdueBucket.TOP_20))
      addAll(mapToSorted(nextPicked, OverdueBucket.NEXT_30))
      addAll(mapToSorted(topRemaining, OverdueBucket.TOP_20))
      addAll(mapToSorted(nextRemaining, OverdueBucket.NEXT_30))
      addAll(mapToSorted(rest, OverdueBucket.REMAINING))
    }
  }
}
