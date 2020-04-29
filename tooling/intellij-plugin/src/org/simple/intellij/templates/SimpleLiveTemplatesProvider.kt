package org.simple.intellij.templates

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

class SimpleLiveTemplatesProvider : DefaultLiveTemplatesProvider {
  override fun getDefaultLiveTemplateFiles(): Array<String> =
      arrayOf("live-templates/simple-org.xml")

  override fun getHiddenLiveTemplateFiles(): Array<String> =
      emptyArray()
}
