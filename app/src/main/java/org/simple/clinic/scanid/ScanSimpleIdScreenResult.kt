package org.simple.clinic.scanid

import java.util.UUID

data class ScanSimpleIdScreenResult(val uuid: UUID) {

  companion object {
    const val KEY = "bp_passport_code_scanned"
  }
}
