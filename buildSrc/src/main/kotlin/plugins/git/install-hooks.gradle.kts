package plugins.git

import org.gradle.internal.os.OperatingSystem

// Git hooks have to be manually copied and made executable. This task automates that.
val gitExecutableHooks: Task = tasks.create("gitExecutableHooks") {
  doLast {
    val operatingSystem = OperatingSystem.current()
    if (operatingSystem.isMacOsX || operatingSystem.isLinux) {
      Runtime.getRuntime().exec("chmod -R +x .git/hooks/")
    }
  }
}

val installGitHooks = tasks.create<Copy>("installGitHooks") {
  from(File("${rootProject.rootDir}/quality", "pre-push"))
  into(File(rootProject.rootDir, ".git/hooks"))
}

tasks.named("preBuild") {
  finalizedBy(installGitHooks)
}
gitExecutableHooks.dependsOn(installGitHooks)
tasks.named("clean") {
  dependsOn(gitExecutableHooks)
}
