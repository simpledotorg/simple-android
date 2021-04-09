package org.simple.clinic.storage.monitoring

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import javax.inject.Inject

class DbMethodLookup @VisibleForTesting(otherwise = PRIVATE) constructor(
    metadataCsv: String
) {

  /*
  * This constructor does load the DB metadata on the main thread when initialised. However, in
  * practice, the impact is relatively light (~5ms on a low end device, Samsung Galaxy M01 Core).
  *
  * Given that the size of this file will not increase by a lot, and the fact that is only done once
  * per launch, I decided to leave it as is because the effort involved in making it asynchronous
  * for something that does not happen more than once per cold start seemed too much.
  **/
  @Inject
  constructor(application: Application): this(
      metadataCsv = application.assets.open("db_metadata.csv").reader().readText()
  )

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
