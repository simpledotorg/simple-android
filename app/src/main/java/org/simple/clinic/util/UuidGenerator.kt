package org.simple.clinic.util

import com.github.f4b6a3.uuid.UuidCreator
import com.github.f4b6a3.uuid.enums.UuidNamespace
import java.util.UUID

fun createUuid5(name: String) : UUID {
  return UuidCreator.getNameBasedSha1(UuidNamespace.NAMESPACE_DNS.value, name)
}
