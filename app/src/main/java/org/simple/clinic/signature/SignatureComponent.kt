package org.simple.clinic.signature

import android.os.Binder
import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface SignatureComponent {

  fun inject(target: SignatureActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): SignatureComponent
  }
}
