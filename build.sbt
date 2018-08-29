
name := "timetable-service"

organization := "com.ferris"

version := "0.0.1"

scalaVersion in ThisBuild := "2.12.1"

/** main project containing main source code depending on slick and codegen project */
lazy val root = (project in file("."))
  .settings(rootSettings)
  .settings(sharedSettings)
  .settings(slick := slickCodeGenTask.value) // register manual sbt command)
  .settings(sourceGenerators in Compile += slickCodeGenTask.taskValue) // register automatic code generation on every compile, remove for only manual use)
  .settings(sourceManaged in Compile <<= baseDirectory { _ / generatedSourcesFolder })
  .dependsOn(codegen)
  .dependsOn(contract)

/** codegen project containing the customized code generator */
lazy val codegen = project
  .settings(sharedSettings)
  .settings(libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % dependencies.slickV)

lazy val contract = (project in file("timetable-rest-contract"))
  .settings(rootSettings)

lazy val client = (project in file("timetable-service-client"))
  .settings(rootSettings)
  .settings(libraryDependencies += "com.ferris" %% "ferris-http-service-client" % dependencies.ferrisClientV)
  .dependsOn(contract)


lazy val rootSettings = {
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

  libraryDependencies ++= {
    Seq(
      "com.typesafe.akka" %% "akka-actor"                 % dependencies.akkaV,
      "com.typesafe.akka" %% "akka-stream"                % dependencies.akkaV,
      "com.typesafe.akka" %% "akka-http-core"             % dependencies.akkaHttpV,
      "com.typesafe.akka" %% "akka-http"                  % dependencies.akkaHttpV,
      "com.typesafe.akka" %% "akka-http-spray-json"       % dependencies.akkaHttpV,
      "com.typesafe.akka" %% "akka-http-jackson"          % dependencies.akkaHttpV,
      "com.typesafe.akka" %% "akka-http-xml"              % dependencies.akkaHttpV,
      "com.typesafe.akka" %% "akka-http-testkit"          % dependencies.akkaHttpV,
      "com.ferris"        %% "ferris-http-microservice"   % dependencies.ferrisMicroserviceV,
      "com.ferris"        %% "ferris-json-utils"          % dependencies.ferrisJsonUtilsV,
      "com.ferris"        %% "ferris-common-utils"        % dependencies.ferrisCommonV,
      "com.ferris"        %% "planning-service"           % dependencies.planningServiceV,
      "org.typelevel"     %% "cats-core"                  % dependencies.catsV,
      "com.rms.miu"       %% "slick-cats"                 % dependencies.slickCatsV,
      "com.github.fommil" %% "spray-json-shapeless"       % dependencies.fommilV,
      "mysql"             %  "mysql-connector-java"       % dependencies.mysqlConnectorV,
      "org.flywaydb"      %  "flyway-core"                % dependencies.flywayV,
      "org.scalatest"     %% "scalatest"                  % dependencies.scalaTestV       % Test,
      "org.mockito"       %  "mockito-all"                % dependencies.mockitoV         % Test
    )
  }
}

// shared sbt config between main project and codegen project
lazy val sharedSettings = Seq(
  scalacOptions := Seq("-feature", "-unchecked", "-deprecation"),
  libraryDependencies ++= Seq(
    "com.typesafe.slick"  %% "slick"          % dependencies.slickV,
    "com.typesafe.slick"  %% "slick-hikaricp" % dependencies.slickV,
    "org.slf4j"           %  "slf4j-nop"      % "1.7.10",
    "com.h2database"      %  "h2"             % "1.4.187"
  )
)

lazy val dependencies = new {
  val akkaV                       = "2.4.16"
  val akkaHttpV                   = "10.0.1"
  val ferrisMicroserviceV         = "0.0.1"
  val ferrisJsonUtilsV            = "0.0.2"
  val ferrisClientV               = "0.0.1"
  val ferrisCommonV               = "0.0.5"
  val planningServiceV            = "0.0.1"
  val catsV                       = "1.2.0"
  val slickCatsV                  = "0.6"
  val slickV                      = "3.2.0-M2"
  val mysqlConnectorV             = "5.1.40"
  val flywayV                     = "3.2.1"
  val scalaTestV                  = "3.0.1"
  val fommilV                     = "1.4.0"
  val mockitoV                    = "1.10.19"
}

lazy val generatedSourcesFolder = "src/generated-sources/scala"

// code generation task that calls the customized code generator
lazy val slick = taskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = Def.task {
  val dir = baseDirectory { _ / generatedSourcesFolder }.value
  val cp = (dependencyClasspath in Compile).value
  val r = (runner in Compile).value
  val s = streams.value
  val outputDir = dir.getPath // place generated files in sbt's managed sources folder
  toError(r.run("com.ferris.codegen.CustomizedCodeGenerator", cp.files, Array(outputDir), s.log))
  val fname = outputDir + "/com/ferris/timetable/table/Tables.scala"
  Seq(file(fname))
}

Revolver.settings
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerExposedPorts := Seq(9000)
dockerEntrypoint := Seq("bin/%s" format executableScriptName.value, "-Dconfig.resource=docker.conf")

flywayUrl := "jdbc:mysql://localhost:3306/timetable"

flywayUser := "root"
