
// --- project ---

name := "FRP_embedded"
version := "1.0"


// --- scala settings ---

scalaVersion := "2.11.2"
scalaOrganization := "org.scala-lang.virtualized"
scalacOptions += "-Yvirtualize"
resolvers += Resolver.sonatypeRepo("snapshots")


// --- dependencies ---

libraryDependencies += "org.scala-lang.lms" %% "lms-core" % "1.0.0-SNAPSHOT"
libraryDependencies += "org.scala-lang.virtualized" % "scala-compiler" % "2.11.2"
libraryDependencies += "org.scala-lang.virtualized" % "scala-library" % "2.11.2"
libraryDependencies += "org.scala-lang.virtualized" % "scala-reflect" % "2.11.2"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.2"
