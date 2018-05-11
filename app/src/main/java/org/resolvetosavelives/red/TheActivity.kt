package org.resolvetosavelives.red

import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers.io
import org.resolvetosavelives.red.home.HomeScreen
import org.resolvetosavelives.red.newentry.search.Patient
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.router.ScreenResultBus
import org.resolvetosavelives.red.router.screen.ActivityResult
import org.resolvetosavelives.red.router.screen.FullScreenKey
import org.resolvetosavelives.red.router.screen.NestedKeyChanger
import org.resolvetosavelives.red.router.screen.ScreenRouter
import java.util.UUID

class TheActivity : AppCompatActivity() {

  // TODO: Remove these once we setup DI.
  companion object {
    @SuppressLint("StaticFieldLeak")
    private lateinit var screenRouter: ScreenRouter

    private lateinit var patientRepository: PatientRepository

    fun screenRouter(): ScreenRouter {
      return screenRouter
    }

    fun patientRepository(): PatientRepository {
      return patientRepository
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val databaseName = getString(R.string.app_name)
    val appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, databaseName).build()
    patientRepository = PatientRepository(appDatabase)

    seedDatabaseWithDummyPatients()
  }

  override fun attachBaseContext(baseContext: Context) {
    screenRouter = ScreenRouter.create(this, NestedKeyChanger(), ScreenResultBus())
    val contextWithRouter = screenRouter.installInContext(baseContext, android.R.id.content, initialScreenKey())
    super.attachBaseContext(contextWithRouter)
  }

  private fun initialScreenKey(): FullScreenKey {
    return HomeScreen.KEY
  }

  // TODO: remove this after we've showcased the app to Dan.
  private fun seedDatabaseWithDummyPatients() {
    patientRepository
        .search("")
        .take(1)
        .filter({ patients -> patients.isEmpty() })
        .flatMapCompletable({ _ ->
          val dummyPatients = listOf(
              Patient(UUID.randomUUID().toString(), "Anish Acharya", "9999999999"),
              Patient(UUID.randomUUID().toString(), "Anshu Acharya", "8888888888"),
              Patient(UUID.randomUUID().toString(), "Amit Acharya", "7899859980"),
              Patient(UUID.randomUUID().toString(), "Anish Acharya", "9535520500")
          )
          Observable.fromIterable(dummyPatients)
              .flatMapCompletable { dummyPatient -> patientRepository.save(patient = dummyPatient) }
        })
        .subscribeOn(io())
        .subscribe()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    screenRouter.sendResultAndPop(ActivityResult(requestCode, resultCode, data))
  }

  override fun onBackPressed() {
    val interceptCallback = screenRouter.offerBackPressToInterceptors()
    if (interceptCallback.intercepted) {
      return
    }
    val popCallback = screenRouter.pop()
    if (popCallback.popped) {
      return
    }
    super.onBackPressed()
  }
}
