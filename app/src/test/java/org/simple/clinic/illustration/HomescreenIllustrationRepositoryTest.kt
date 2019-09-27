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
import java.io.FileNotFoundException

class HomescreenIllustrationRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userClock = TestUserClock()
  private val illustrationDao: HomescreenIllustration.RoomDao = mock()
  private val fileStorage: FileStorage = mock()
  private val chosenFile: File = mock()
  private val eventId = "world_heart_day"
  private val illustrations = listOf(
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
  )
  private val repository = HomescreenIllustrationRepository(
      userClock = userClock,
      fileStorage = fileStorage,
      illustrationsFolder = "illustrations-folder/"
  )

  @Before
  fun setUp() {
    whenever(illustrationDao.illustrations()).thenReturn(Observable.just(illustrations))
    whenever(fileStorage.getFile(eventId)).thenReturn(GetFileResult.Success(chosenFile))
  }

  @Test
  fun `verify correct illustration is picked up`() {
    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 18))
    repository.illustrationImageToShow()
        .test()
        .assertValues(chosenFile)
  }

  @Test
  fun `verify illustration is picked up when today is "from" date`() {
    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 10))
    repository.illustrationImageToShow()
        .test()
        .assertValues(chosenFile)
  }

  @Test
  fun `verify illustration is picked up when today is "to" date`() {
    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 20))
    repository.illustrationImageToShow()
        .test()
        .assertValues(chosenFile)
  }

  @Test
  fun `verify illustration is not set if there is no event today`() {
    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 21))
    repository.illustrationImageToShow()
        .test()
        .assertNoValues()
        .assertNoErrors()
  }

  @Test
  fun `verify illustration is not set when there is event today but no illustration file`() {
    whenever(fileStorage.getFile(eventId)).thenReturn(GetFileResult.Failure(FileNotFoundException()))

    userClock.setDate(LocalDate.of(2019, Month.SEPTEMBER, 20))

    repository.illustrationImageToShow()
        .test()
        .assertNoValues()
        .assertNoErrors()
  }
}
