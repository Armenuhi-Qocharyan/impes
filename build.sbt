name := "impes"

version := "0.1"

scalaVersion := "2.12.9"

val sparkVersion = "2.4.3"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,

  "mysql" % "mysql-connector-java" % "8.0.17",

  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "com.typesafe.akka" %% "akka-stream" % "2.4.17",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5",

  "com.typesafe.slick" %% "slick" % "3.2.0",

  "org.xerial" % "sqlite-jdbc" % "3.16.1"
)