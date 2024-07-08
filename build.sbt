ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "akka-http-lowLevelApi",
    idePackagePrefix := Some("com.codinoverse")
  )
libraryDependencies++=Seq(
  //Akka-Actors
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  //Akka-Http
  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-stream" % "2.6.20",
  // Akka HTTP JSON Support (for JSON marshalling/unmarshalling)
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10",
  "ch.megard" %% "akka-http-cors" % "1.2.0",
  //Test
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.20",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.10",
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0" % Test

)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @_*) => MergeStrategy.discard
  case _                           => MergeStrategy.last
}
run / compile in Compile := (run / compile in Compile).dependsOn(compile in Test).value
run / mainClass := Some("CodinoverseBookStoreApp")