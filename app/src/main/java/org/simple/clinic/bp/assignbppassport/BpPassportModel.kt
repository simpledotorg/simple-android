package org.simple.clinic.bp.assignbppassport

import org.simple.clinic.patient.businessid.Identifier

data class BpPassportModel(
    val identifier: Identifier
) {

  companion object {
    fun create(identifier: Identifier): BpPassportModel {
      return BpPassportModel(
          identifier = identifier
      )
    }
  }
}
