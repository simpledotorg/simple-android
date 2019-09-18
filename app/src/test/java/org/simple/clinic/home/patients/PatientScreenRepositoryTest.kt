package org.simple.clinic.home.patients

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.home.patients.illustration.DayOfMonth
import org.simple.clinic.home.patients.illustration.HomescreenIllustration
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.files.GetFileResult
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import java.io.File

class PatientScreenRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  val userClock = TestUserClock()

  @Test
  fun `verify correct illustration is picked up`() {
    val illustrationDao: HomescreenIllustration.RoomDao = mock()
    val fileStorage: FileStorage = mock()
    val eventId = "world_heart_day"
    val chosenFile: File = mock()

    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 18))

    val repository = PatientScreenRepository(
        illustrationDao = illustrationDao,
        fileStorage = fileStorage,
        userClock = userClock
    )
    whenever(illustrationDao.illustrations()).thenReturn(Observable.just(listOf(
        HomescreenIllustration(
            eventId = "event-1",
            illustrationUrl = "some-illustration-url-1",
            from = DayOfMonth(10, Month.AUGUST),
            to = DayOfMonth(20, Month.AUGUST)
        ),
        HomescreenIllustration(
            eventId = eventId,
            illustrationUrl = "some-illustration-url-2",
            from = DayOfMonth(10, Month.SEPTEMBER),
            to = DayOfMonth(20, Month.SEPTEMBER)
        ),
        HomescreenIllustration(
            eventId = "event-2",
            illustrationUrl = "some-illustration-url-3",
            from = DayOfMonth(10, Month.OCTOBER),
            to = DayOfMonth(20, Month.OCTOBER)
        )
    )))
    whenever(fileStorage.getFile(eventId)).thenReturn(GetFileResult.Success(chosenFile))

    repository.illustrations()
        .test()
        .assertValues(GetFileResult.Success(chosenFile))
  }
}
