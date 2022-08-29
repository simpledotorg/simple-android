#!/usr/bin/env kscript

import java.io.File
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.system.exitProcess

val workingDir = File(System.getProperty("user.dir"))
workingDir.run {
  val userName = args[0]
  val userEmail = args[1]

  runCommand("git config user.name $userName")
  runCommand("git config user.email $userEmail")

  if (isGitDirty()) {
    val date = LocalDate.now()
    val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.US)
    val branchDate = "${date.dayOfMonth}$monthName"
    val branchName = "bot/$branchDate/code-formatting"
    val commitName = "[BOT] Code formatting fixes"

    runCommand("git checkout -b $branchName")
    runCommand("git commit -am $commitName")
    runCommand("git push -u origin $branchName")
  } else {
    println("No changes in the current directory!")
  }

  exitProcess(0)
}

fun File.isGitDirty(): Boolean {
  val gitDiffExitValue = runCommand("git diff --quiet").run {
    exitValue()
  }
  return gitDiffExitValue == 1
}

fun File.runCommand(command: String): Process {
  val parts = command.split("\\s".toRegex())

  return ProcessBuilder(*parts.toTypedArray())
      .directory(this)
      .start()
      .apply { waitFor() }
}
