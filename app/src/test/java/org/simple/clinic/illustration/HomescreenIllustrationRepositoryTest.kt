package org.simple.clinic.illustration

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.files.GetFileResult
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import java.io.File

class HomescreenIllustrationRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userClock = TestUserClock()
  private val illustrationDao: HomescreenIllustration.RoomDao = mock()
  private val fileStorage: FileStorage = mock()
  private val chosenFile: File = mock()
  private val repository = HomescreenIllustrationRepository(
      illustrationDao = illustrationDao,
      fileStorage = fileStorage,
      userClock = userClock
  )

  @Before
  fun setUp() {
    val eventId = "world_heart_day"

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
  }

  @Test
  fun `verify correct illustration is picked up`() {
    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 18))
    repository.illustrations()
        .test()
        .assertValues(chosenFile)
  }

  @Test
  fun `verify illustration is picked up when today is "from" date`() {
    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 10))
    repository.illustrations()
        .test()
        .assertValues(chosenFile)
  }

  @Test
  fun `verify illustration is picked up when today is "to" date`() {
    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 20))
    repository.illustrations()
        .test()
        .assertValues(chosenFile)
  }
}
