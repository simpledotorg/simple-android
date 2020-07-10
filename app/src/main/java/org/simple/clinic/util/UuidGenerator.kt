package org.simple.clinic.util

import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.impl.NameBasedGenerator
import java.time.LocalDate
import java.util.UUID

private fun createUuid5(name: String): UUID {
  val nameBasedGenerator = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_DNS)
  return nameBasedGenerator.generate(name)
}

fun generateEncounterUuid(facilityUuid: UUID, patientUuid: UUID, encounteredDate: LocalDate): UUID {
  val uuidName = facilityUuid.toString() + patientUuid + encounteredDate
  return createUuid5(uuidName)
}
