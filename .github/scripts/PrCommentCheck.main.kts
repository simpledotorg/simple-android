#!/usr/bin/env kscript

import kotlin.system.exitProcess

val prDescription = args.firstOrNull() ?: "No arguments"
val storyRegexPattern = System.getenv("STORY_REGEX_PATTERN")!!
val storyRegex = Regex(storyRegexPattern)

println("Checking PR description!")
checkPrDescription(prDescription)

fun checkPrDescription(description: String) {
  if (description.contains(storyRegex)) {
    println("PR description contains a valid story link.")
    exitProcess(0)
  } else {
    println("PR description doesn't contain any valid story links.")
    exitProcess(1)
  }
}
