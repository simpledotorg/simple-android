package org.simple.clinic.home.patients

import io.reactivex.Observable
import org.simple.clinic.home.patients.illustration.HomescreenIllustration
import org.simple.clinic.storage.files.FileStorage
import javax.inject.Inject

class PatientScreenRepository @Inject constructor(
    private val illustrationDao: HomescreenIllustration.RoomDao,
    private val fileStorage: FileStorage
) {

  fun illustrations(): Observable<HomescreenIllustration> =
      illustrationDao.illustrations()
          .filter { it.isNotEmpty() }
          .map { it.first() }
}
