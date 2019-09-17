package org.simple.clinic.home.patients

import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.home.patients.illustration.HomescreenIllustration
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.files.GetFileResult
import javax.inject.Inject

class PatientScreenRepository @Inject constructor(
    private val illustrationDao: HomescreenIllustration.RoomDao,
    private val fileStorage: FileStorage
) {

  fun illustrations(): Observable<GetFileResult.Success> =
      illustrationDao.illustrations()
          .filter { it.isNotEmpty() }
          .map { pickIllustration(it) }
          .map { fileStorage.getFile(it.eventId) }
          .ofType()

  private fun pickIllustration(illustrations: List<HomescreenIllustration>): HomescreenIllustration =
      illustrations.first()
}
