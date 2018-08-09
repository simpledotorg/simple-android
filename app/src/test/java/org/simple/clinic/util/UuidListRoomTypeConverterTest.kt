package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.UUID

class UuidListRoomTypeConverterTest {

  @Test
  fun `conversion should happen correctly`() {
    val converter = UuidListRoomTypeConverter()

    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()
    val uuids = listOf(uuid1, uuid2)

    val serialized = converter.fromUuids(uuids)
    val deserialized = converter.toUuids(serialized)

    assertThat(deserialized!![0]).isEqualTo(uuid1)
    assertThat(deserialized[1]).isEqualTo(uuid2)
  }
}
