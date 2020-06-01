package org.simple.clinic.uuid

import java.util.UUID

interface UuidGenerator {
  fun v4(): UUID
}
