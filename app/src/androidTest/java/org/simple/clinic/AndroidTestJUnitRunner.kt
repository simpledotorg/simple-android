package org.simple.clinic

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Runs [TestClinicApp] instead of the actual Application class in Android tests.
 * This class is declared in app/build.gradle as the project's default testInstrumentationRunner.
 */
@Suppress("unused")
class AndroidTestJUnitRunner : AndroidJUnitRunner() {

  @Throws(InstantiationException::class, IllegalAccessException::class, ClassNotFoundException::class)
  override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
    return super.newApplication(cl, TestClinicApp::class.java.name, context)
  }
}
