package org.simple.clinic.uuid

import java.util.UUID
import javax.inject.Inject

class RealUuidGenerator @Inject constructor() : UuidGenerator {

  override fun v4(): UUID {
    return UUID.randomUUID()
  }
}
