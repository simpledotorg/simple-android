package org.simple.clinic.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.debug.await_activity.*
import org.simple.clinic.R
import org.simple.clinic.await.Await
import org.simple.clinic.await.Checkpoint
import org.simple.clinic.util.unsafeLazy

class AwaitActivity : AppCompatActivity() {
  private var awaitDisposable: Disposable? = null

  private val await by unsafeLazy {
    val checkpoints = listOf(
        Checkpoint("Wait, one sec...", 0),
        Checkpoint("Sorry, we literally meant one second!", 2000),
        Checkpoint("This is taking longer than we expected, it'll take a moment...", 5000),
        Checkpoint("NVM, it's on us. Please go grab a coffee.", 15000)
    )
    Await(checkpoints)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.await_activity)
  }

  override fun onStart() {
    super.onStart()
    awaitDisposable = await.items()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { message -> messageTextView.text = message }
  }

  override fun onStop() {
    awaitDisposable?.dispose()
    super.onStop()
  }
}
