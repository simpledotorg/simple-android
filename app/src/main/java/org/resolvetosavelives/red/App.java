package org.resolvetosavelives.red;

import android.app.Application;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    Timber.plant(new DebugTree());
  }
}
