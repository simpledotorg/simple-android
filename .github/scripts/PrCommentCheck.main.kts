#!/usr/bin/env kscript

import kotlin.system.exitProcess

val prDescription = args.firstOrNull().orEmpty()
val storyRegex = Regex("https:\\/\\/app.shortcut.com\\/simpledotorg\\/story\\/[0-9]+\\/[A-Za-z0-9_-]+")

println("Checking PR description!")

if (prDescription.contains(storyRegex)) {
  println("PR description contains a valid story link.")
  exitProcess(0)
} else {
  println("PR description doesn't contain any valid story links.")
  exitProcess(1)
}
