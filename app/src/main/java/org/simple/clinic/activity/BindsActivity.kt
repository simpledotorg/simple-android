package org.simple.clinic.activity

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance

interface BindsActivity<B> {

  @BindsInstance
  fun activity(activity: AppCompatActivity): B
}
