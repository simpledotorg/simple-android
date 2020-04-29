package org.simple.intellij.macros

import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.macro.SimpleMacro
import java.util.UUID

class UuidMacro : SimpleMacro("uuid") {
  override fun evaluateSimpleMacro(
      params: Array<out Expression>,
      context: ExpressionContext
  ): String = "\"${UUID.randomUUID()}\""
}
