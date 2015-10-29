lazy val root = (project in file(".")).
  settings(
    name := "compiled-query",
    version := "1.0",
    scalaVersion := "2.11.7",

    resolvers += "Sonatype OSS Snapshots" at
      "https://oss.sonatype.org/content/repositories/snapshots",

    libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.3",
    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
    libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.7",
    libraryDependencies += "asm" % "asm-all" % "3.3.1",
    libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.8",



    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),

    parallelExecution in Test := false
  )

