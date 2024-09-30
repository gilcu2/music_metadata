ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

val Http4sVersion = "0.23.16"
val PureConfigVersion = "0.17.7"
val CirceVersion = "0.14.9"
val DoobieVersion = "1.0.0-RC3"
val H2Version = "2.3.232"
val FlywayVersion = "10.18.2"
val ScalaTestVersion = "3.2.19"


lazy val root = (project in file("."))
  .settings(
    name := "music_metadata",
      libraryDependencies ++= Seq(
        "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
        "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
        "org.http4s" %% "http4s-circe" % Http4sVersion,
        "org.http4s" %% "http4s-dsl" % Http4sVersion,
        "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
        "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
        "io.circe" %% "circe-generic" % CirceVersion,
        "io.circe" %% "circe-parser" % CirceVersion,
        "org.tpolecat" %% "doobie-h2" % DoobieVersion,
        "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
        "de.lhns" %% "doobie-flyway" % "0.4.0",
        "com.h2database" % "h2" % H2Version,
        "org.flywaydb" % "flyway-core" % FlywayVersion,

        "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
        "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
      )
  )

