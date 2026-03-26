package org.simple.clinic.home.overdue

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.simple.clinic.TestData
import org.simple.clinic.TestData.overdueAppointment
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.returnscore.LikelyToReturnIfNotCalledIn15DaysScoreType
import org.simple.clinic.returnscore.ReturnScore
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

class OverdueAppointmentSorterTest {

  private lateinit var returnScoreDao: ReturnScore.RoomDao
  private lateinit var features: Features
  private lateinit var sorter: OverdueAppointmentSorter

  @Before
  fun setup() {
    returnScoreDao = mock()
    features = mock()

    sorter = OverdueAppointmentSorter(
        returnScoreDao = returnScoreDao,
        features = features,
        random = Random(123)
    )
  }

  @Test
  fun `returns original list when feature disabled`() {
    val list = (1..5).map { overdueAppointment(UUID.randomUUID()) }

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(false)

    val result = sorter.sort(list)

    assertThat(result).containsExactlyElementsIn(list).inOrder()
  }

  @Test
  fun `does not lose or duplicate patients`() {
    val list = (1..20).map { overdueAppointment(UUID.randomUUID()) }

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    whenever(returnScoreDao.getAllImmediate()).thenReturn(emptyList())

    val result = sorter.sort(list)

    val input = list.map { it.appointment.patientUuid }
    val output = result.map { it.appointment.appointment.patientUuid }

    assertThat(output).containsExactlyElementsIn(input)
  }

  @Test
  fun `higher score patients appear before lower score patients`() {
    val p1 = UUID.randomUUID()
    val p2 = UUID.randomUUID()

    val list = listOf(
        overdueAppointment(patientUuid = p1),
        overdueAppointment(patientUuid = p2)
    )

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    whenever(returnScoreDao.getAllImmediate()).thenReturn(
        listOf(
            TestData.returnScore(patientUuid = p1, scoreValue = 10f),
            TestData.returnScore(patientUuid = p2, scoreValue = 50f),
        )
    )

    val result = sorter.sort(list)
    val uuids = result.map { it.appointment.appointment.patientUuid }

    assertThat(uuids.indexOf(p2)).isLessThan(uuids.indexOf(p1))
  }

  @Test
  fun `same seed produces same result`() {
    val uuids = (1..10).map { UUID.randomUUID() }
    val list = uuids.map { overdueAppointment(patientUuid = it) }

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    val scores = uuids.mapIndexed { index, uuid ->
      TestData.returnScore(patientUuid = uuid, scoreValue = (100 - index).toFloat())
    }

    whenever(returnScoreDao.getAllImmediate()).thenReturn(scores)

    val sorter1 = OverdueAppointmentSorter(returnScoreDao, features, Random(123))
    val sorter2 = OverdueAppointmentSorter(returnScoreDao, features, Random(123))

    val result1 = sorter1.sort(list)
    val result2 = sorter2.sort(list)

    assertThat(result1.map { it.appointment.appointment.patientUuid })
        .containsExactlyElementsIn(result2.map { it.appointment.appointment.patientUuid })
        .inOrder()
  }

  @Test
  fun `sorts by score descending`() {
    val p1 = UUID.randomUUID()
    val p2 = UUID.randomUUID()
    val p3 = UUID.randomUUID()

    val list = listOf(
        overdueAppointment(patientUuid = p1),
        overdueAppointment(patientUuid = p2),
        overdueAppointment(patientUuid = p3)
    )

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    whenever(returnScoreDao.getAllImmediate()).thenReturn(
        listOf(
            TestData.returnScore(patientUuid = p1, scoreValue = 10f),
            TestData.returnScore(patientUuid = p2, scoreValue = 50f),
            TestData.returnScore(patientUuid = p3, scoreValue = 30f),
        )
    )

    val result = sorter.sort(list)

    val uuids = result.map { it.appointment.appointment.patientUuid }

    assertThat(uuids.indexOf(p2)).isLessThan(uuids.indexOf(p1))
    assertThat(uuids.indexOf(p3)).isLessThan(uuids.indexOf(p1))
  }

  @Test
  fun `ignores non LikelyToReturnIfCalled score types`() {
    val uuid = UUID.randomUUID()

    val list = listOf(overdueAppointment(patientUuid = uuid))

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    whenever(returnScoreDao.getAllImmediate()).thenReturn(
        listOf(
            ReturnScore(
                uuid = UUID.randomUUID(),
                patientUuid = uuid,
                scoreType = LikelyToReturnIfNotCalledIn15DaysScoreType,
                scoreValue = 100f,
                timestamps = Timestamps(
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    deletedAt = null
                )
            ),
            TestData.returnScore(uuid, scoreValue = 10f)
        )
    )

    val result = sorter.sort(list)

    val first = result.first().appointment.appointment.patientUuid

    assertThat(first).isEqualTo(uuid)
  }

  @Test
  fun `uses default score when missing`() {
    val p1 = UUID.randomUUID()
    val p2 = UUID.randomUUID()

    val list = listOf(
        overdueAppointment(patientUuid = p1),
        overdueAppointment(patientUuid = p2)
    )

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    whenever(returnScoreDao.getAllImmediate()).thenReturn(
        listOf(TestData.returnScore(patientUuid = p1, scoreValue = 50f))
    )

    val result = sorter.sort(list)

    val sorted = result.map { it.appointment.appointment.patientUuid }

    assertThat(sorted.first()).isEqualTo(p1)
  }

  @Test
  fun `picks patients only from top 20 and next 30 buckets`() {
    val uuids = (1..10).map { UUID.randomUUID() }
    val list = uuids.map { overdueAppointment(patientUuid = it) }

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    val scores = uuids.mapIndexed { index, uuid ->
      TestData.returnScore(patientUuid = uuid, scoreValue = (100 - index).toFloat())
    }

    whenever(returnScoreDao.getAllImmediate()).thenReturn(scores)

    val result = sorter.sort(list)

    val top20 = uuids.take(2)
    val next30 = uuids.subList(2, 5)

    val picked = result.take(2).map { it.appointment.appointment.patientUuid }

    picked.forEach {
      assertThat(it in (top20 + next30)).isTrue()
    }
  }

  @Test
  fun `handles single item safely`() {
    val uuid = UUID.randomUUID()

    val list = listOf(overdueAppointment(patientUuid = uuid))

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    whenever(returnScoreDao.getAllImmediate()).thenReturn(
        listOf(TestData.returnScore(patientUuid = uuid, scoreValue = 10f))
    )

    val result = sorter.sort(list)

    assertThat(result).hasSize(1)
  }

  @Test
  fun `all patients get default score when no valid score type`() {
    val uuids = (1..5).map { UUID.randomUUID() }
    val list = uuids.map { overdueAppointment(patientUuid = it) }

    whenever(features.isEnabled(Feature.SortOverdueBasedOnReturnScore))
        .thenReturn(true)

    whenever(returnScoreDao.getAllImmediate()).thenReturn(
        uuids.map {
          ReturnScore(
              uuid = UUID.randomUUID(),
              patientUuid = it,
              scoreType = LikelyToReturnIfNotCalledIn15DaysScoreType,
              scoreValue = 100f,
              timestamps = Timestamps(
                  createdAt = Instant.now(),
                  updatedAt = Instant.now(),
                  deletedAt = null
              )
          )
        }
    )

    val result = sorter.sort(list)

    assertThat(result).hasSize(list.size)
  }
}
