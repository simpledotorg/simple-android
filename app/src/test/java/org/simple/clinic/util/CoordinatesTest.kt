package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.location.Coordinates

@RunWith(JUnitParamsRunner::class)
class CoordinatesTest {

  @Test
  @Parameters(method = "params for coordinates and distance")
  fun `haversine formula should work correctly`(
      start: Coordinates,
      end: Coordinates,
      expectedDistance: Distance
  ) {

    val oneWay = Coordinates.haversineDistance(start, end)
    val returnTrip = Coordinates.haversineDistance(end, start)

    assertThat(oneWay).isEqualTo(expectedDistance)
    assertThat(returnTrip).isEqualTo(expectedDistance)
  }

  @Suppress("unused")
  fun `params for coordinates and distance`(): List<List<Any>> {
    val start1 = Coordinates(latitude = 1.908537, longitude = 73.537524)
    val end1 = Coordinates(latitude = 59.299800, longitude = 18.209118)
    val expectedDistance1 = Distance.ofKilometers(7939.655950402867)

    val start2 = Coordinates(latitude = 12.969858, longitude = 77.611767)
    val end2 = Coordinates(latitude = 12.971215, longitude = 77.612058)
    val expectedDistance2 = Distance.ofMeters(154.151)

    val start3 = Coordinates(latitude = 43.431554, longitude = -80.470919)
    val end3 = Coordinates(latitude = -45.662004, longitude = -67.531463)
    val expectedDistance3 = Distance.ofKilometers(9988.876661)

    return listOf(
        listOf(start1, end1, expectedDistance1),
        listOf(start2, end2, expectedDistance2),
        listOf(start3, end3, expectedDistance3))
  }
}
