package org.simple.clinic.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class MobiusDispatchEventDetector : Detector(), SourceCodeScanner {

  companion object {
    val ISSUE: Issue = Issue.create(
        id = "MobiusDispatchEventWarning",
        briefDescription = "This method is deprecated and should not be used. Use `dispatch` instead",
        explanation = "This method is deprecated and should not be used. Use `dispatch` instead",
        category = Category.CORRECTNESS,
        severity = Severity.INFORMATIONAL,
        implementation = Implementation(
            MobiusDispatchEventDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        ),
    )
  }

  override fun getApplicableMethodNames(): List<String> {
    return listOf("dispatchEvent")
  }

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    if (method.containingClass?.qualifiedName == "com.spotify.mobius.android.MobiusLoopViewModel") {
      context.report(
          ISSUE,
          node,
          context.getLocation(node),
          "Avoid calling viewModel.dispatchEvent"
      )
    }
  }
}
