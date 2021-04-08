package org.simple.clinic.storage.monitoring

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE

class DbMethodLookup @VisibleForTesting(otherwise = PRIVATE) constructor(
    metadataCsv: String
) {

  private val allDaoMethods: List<DaoMetadata> = DaoMetadata.parse(metadataCsv)

  fun find(daoName: String, lineNumber: Int): String? {
    return allDaoMethods
        .find { metadata -> metadata.matches(daoName, lineNumber) }
        ?.methodName
  }

  private data class DaoMetadata(
      val daoName: String,
      val methodName: String,
      val methodRange: IntRange
  ) {

    fun matches(daoName: String, lineNumber: Int): Boolean {
      return this.daoName == daoName && lineNumber in methodRange
    }

    companion object {
      fun parse(csv: String): List<DaoMetadata> {
        return csv
            .split('\n')
            .asSequence()
            .filterNot { it.isBlank() }
            .map { it.split(',') }
            .map { (dao, method, start, end) ->
              DaoMetadata(
                  daoName = dao,
                  methodName = method,
                  methodRange = start.toInt()..end.toInt()
              )
            }
            .toList()
      }
    }
  }
}
