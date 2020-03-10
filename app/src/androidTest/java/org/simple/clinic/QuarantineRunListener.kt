package org.simple.clinic

import android.app.Instrumentation
import android.os.Bundle
import androidx.test.internal.runner.listener.InstrumentationRunListener
import androidx.test.platform.app.InstrumentationRegistry
import com.vinaysshenoy.quarantine.InMemoryTestRepository
import org.junit.runner.Result
import org.simple.clinic.util.Rules
import java.io.PrintStream

class QuarantineRunListener : InstrumentationRunListener() {

  override fun setInstrumentation(instr: Instrumentation) {
    super.setInstrumentation(instr)
    Rules.overrideQuarantineClassloader(instr.context.classLoader)
  }

  override fun instrumentationRunFinished(
      streamResult: PrintStream,
      resultBundle: Bundle,
      junitResults: Result
  ) {
    val repository = InMemoryTestRepository.instance(instrumentation.context.classLoader)
    if(repository.config().enabled) {
      repository.pushResultsToCloud()
    }
  }
}
