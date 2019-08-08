package org.simple.intellij.templates

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile

class SimpleTemplateContextType : TemplateContextType("org.simple.intellij.templates", "Simple.org") {
  override fun isInContext(file: PsiFile, offset: Int): Boolean = true
}
