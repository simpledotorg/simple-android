package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.location.Coordinates

class CoordinatesTest {

  @Test
  fun `haversine formula should work correctly`() {
    val start = Coordinates(latitude = 1.908537, longitude = 73.537524)
    val end = Coordinates(latitude = 59.299800, longitude = 18.209118)
    val maldivesToStockholm = Coordinates.haversineDistance(start, end)
    val stockholmToMaldives = Coordinates.haversineDistance(start, end)

    assertThat(maldivesToStockholm).isEqualTo(Distance.ofKilometers(7939.655950402867))
    assertThat(stockholmToMaldives).isEqualTo(Distance.ofKilometers(7939.655950402867))
  }
}
