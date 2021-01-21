package org.simple.clinic.signature

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface SignatureComponent {

  fun inject(target: SignatureActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): SignatureComponent
  }
}
