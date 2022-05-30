package org.simple.sharedTestCode.uuid

import org.simple.clinic.uuid.UuidGenerator
import java.util.UUID

class FakeUuidGenerator(
    private val uuid: UUID
) : UuidGenerator {

  companion object {
    fun fixed(uuid: UUID): UuidGenerator = FakeUuidGenerator(uuid)
  }

  override fun v4(): UUID {
    return uuid
  }
}
