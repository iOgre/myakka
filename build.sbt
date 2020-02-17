name := "myakka"

version := "0.1"

scalaVersion := "2.13.1"

val akkaVersion = "2.5.23"
val logbackVersion = "1.2.3"
val hbaseVersion = "1.4.6"
val phoenixVersion = "4.14.0-HBase-1.4"
val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion)