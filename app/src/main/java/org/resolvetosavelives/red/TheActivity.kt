package org.resolvetosavelives.red

import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.resolvetosavelives.red.home.HomeScreen
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.router.ScreenResultBus
import org.resolvetosavelives.red.router.screen.ActivityResult
import org.resolvetosavelives.red.router.screen.FullScreenKey
import org.resolvetosavelives.red.router.screen.NestedKeyChanger
import org.resolvetosavelives.red.router.screen.ScreenRouter

class TheActivity : AppCompatActivity() {

  // TODO: Remove these once we setup DI.
  companion object {
    @SuppressLint("StaticFieldLeak")
    private lateinit var screenRouter: ScreenRouter

    private lateinit var patientRepository: PatientRepository
    private lateinit var appDatabase: AppDatabase

    fun screenRouter(): ScreenRouter {
      return screenRouter
    }

    fun patientRepository(): PatientRepository {
      return patientRepository
    }

    fun appDatabase(): AppDatabase {
      return appDatabase
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val databaseName = getString(R.string.app_name)
    appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, databaseName)
        .allowMainThreadQueries()   // We use RxJava for threading.
        .build()
    patientRepository = PatientRepository(appDatabase)
  }

  override fun attachBaseContext(baseContext: Context) {
    screenRouter = ScreenRouter.create(this, NestedKeyChanger(), ScreenResultBus())
    val contextWithRouter = screenRouter.installInContext(baseContext, android.R.id.content, initialScreenKey())
    super.attachBaseContext(contextWithRouter)
  }

  private fun initialScreenKey(): FullScreenKey {
    return HomeScreen.KEY
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
