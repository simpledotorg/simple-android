package org.simple.clinic.util

import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.impl.NameBasedGenerator
import org.threeten.bp.LocalDate
import java.util.UUID

fun createUuid5(name: String): UUID {
  val nameBasedGenerator = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_DNS)
  return nameBasedGenerator.generate(name)
}
