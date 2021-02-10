name := "AOP"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies += compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.2" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "org.tpolecat"  %% "natchez-datadog" % "0.0.19",
  "ch.qos.logback"       % "logback-classic"          % "1.2.3",
  "net.logstash.logback" % "logstash-logback-encoder" % "6.4",
  "org.tpolecat" %% "natchez-log-odin" % "0.0.19"
)

scalacOptions ++= Seq("-language:higherKinds", "-Ydelambdafy:inline", "-Ymacro-annotations")