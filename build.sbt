
ThisBuild / scalaVersion := "2.13.3"

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  isSnapshot.value match {
    case true => Some("snapshots" at nexus + "content/repositories/snapshots")
    case false => Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
}
publishArtifact := false

val commonSettings = Seq(
  organization := "com.github.nawforce",
  name := "scala-json-rpc",
  version := "1.0.0",
  scalaVersion := "2.13.3",
  logBuffered in Test := false,
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/nawforce/scala-json-rpc")),
  credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    isSnapshot.value match {
      case true => Some("snapshots" at nexus + "content/repositories/snapshots")
      case false => Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  },
  publishArtifact := false,
  pomExtra := <scm>
    <url>git@github.com:nawforce/scala-json-rpc.git</url>
    <connection>scm:git:git@github.com:nawforce/scala-json-rpc.git</connection>
  </scm>
      <developers>
        <developer>
          <id>shogowada</id>
          <name>Shogo Wada</name>
          <url>https://github.com/shogowada</url>
        </developer>
      </developers>
)

val Version = new {
  val circe = "0.13.0"
}

lazy val core = crossProject(JSPlatform, JVMPlatform).in(file("."))
    .disablePlugins(AssemblyPlugin)
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,

        "com.lihaoyi" %%% "upickle" % "1.2.0",

        "org.scalatest" %%% "scalatest" % "3.2.0" % Test,
        "org.scalatest" %%% "scalatest-freespec" % "3.2.0" % Test,
        "org.scalatest" %%% "scalatest-shouldmatchers" % "3.2.0" % Test,
        "org.scalacheck" %% "scalacheck" % "1.14+" % Test
      ),
      publishArtifact := true
    )

lazy val upickleJSONSerializer = crossProject(JSPlatform, JVMPlatform).in(file("upickle-json-serializer"))
    .disablePlugins(AssemblyPlugin)
    .settings(commonSettings: _*)
    .settings(
      name += "-upickle-json-serializer",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,

        "com.lihaoyi" %%% "upickle" % "1.2.0",
        "org.scalatest" %%% "scalatest" % "3.2.0" % Test,
        "org.scalatest" %%% "scalatest-freespec" % "3.2.0" % Test,
        "org.scalatest" %%% "scalatest-shouldmatchers" % "3.2.0" % Test,
      ),
      publishArtifact := true
    )
    .dependsOn(core)

lazy val circeJSONSerializer = crossProject(JSPlatform, JVMPlatform).in(file("circe-json-serializer"))
    .disablePlugins(AssemblyPlugin)
    .settings(commonSettings: _*)
    .settings(
      name += "-circe-json-serializer",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "io.circe" %%% "circe-parser" % Version.circe,
        "io.circe" %%% "circe-core" % Version.circe,
        "io.circe" %%% "circe-generic" % Version.circe % "test",
        "org.scalatest" %%% "scalatest" % "3.2.0" % "test",
        "org.scalatest" %%% "scalatest-freespec" % "3.2.0" % Test,
        "org.scalatest" %%% "scalatest-shouldmatchers" % "3.2.0" % Test,
        "org.scalacheck" %% "scalacheck" % "1.14+" % "test"
      ),
      publishArtifact := true
    )
    .dependsOn(core)

// Examples
/* Disable examples for now while upgrading core

lazy val JettyVersion = "9.+"

lazy val exampleCommonSettings = Seq(
  name += "-example",
  publishArtifact := false
)

lazy val exampleJvmCommonSettings = Seq(
  //pipelineStages in Assets := Seq(scalaJSDev),
  WebKeys.packagePrefix in Assets := "public/",
  managedClasspath in Runtime += (packageBin in Assets).value,
  libraryDependencies ++= Seq(
    "org.eclipse.jetty" % "jetty-webapp" % JettyVersion,
    "org.scalatra" %% "scalatra" % "2.5.+",

    "org.seleniumhq.selenium" % "selenium-java" % "[3.4.0,4.0.0[" % "it",
    "org.scalatest" %%% "scalatest" % "3.2.0" % "it",
    "org.scalacheck" %% "scalacheck" % "1.14+" % "it"
  )
)

lazy val exampleJsCommonSettings = Seq(
  libraryDependencies ++= Seq(
    "io.github.shogowada" %%% "scalajs-reactjs" % "0.11.+",
    "org.scala-js" %%% "scalajs-dom" % "0.9.+"
  )
)

// HTTP example

lazy val exampleE2e = crossProject(JSPlatform, JVMPlatform).in(file("examples/e2e"))
    .settings(commonSettings: _*)
    .settings(exampleCommonSettings: _*)
    .settings(
      name += "-e2e"
    )
    .dependsOn(core, circeJSONSerializer)

lazy val exampleE2eJvm = exampleE2e.jvm
    .enablePlugins(SbtWeb, WebScalaJSBundlerPlugin)
    .configs(IntegrationTest)
    .settings(exampleJvmCommonSettings: _*)
    .settings(Defaults.itSettings: _*)
    .settings(
      scalaJSProjects := Seq(exampleE2eJs),
      unmanagedResourceDirectories in Assets ++= Seq(
        (baseDirectory in exampleE2eJs).value / "src" / "main" / "public"
      ),
      (fork in IntegrationTest) := true,
      (javaOptions in IntegrationTest) ++= Seq(
        s"-DjarLocation=${assembly.value}"
      )
    )
    .dependsOn(exampleTestUtils.jvm % "it")

lazy val exampleE2eJs = exampleE2e.js
    .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
    .disablePlugins(AssemblyPlugin)
    .settings(exampleJsCommonSettings: _*)
    .settings(
      scalaJSUseMainModuleInitializer := true
    )

// WebSocket example

lazy val exampleE2eWebSocket = crossProject(JSPlatform, JVMPlatform).in(file("examples/e2e-web-socket"))
    .settings(commonSettings: _*)
    .settings(exampleCommonSettings: _*)
    .settings(
      name += "-e2e-websocket"
    )
    .dependsOn(core, upickleJSONSerializer)

lazy val exampleE2eWebSocketJvm = exampleE2eWebSocket.jvm
    .enablePlugins(SbtWeb, WebScalaJSBundlerPlugin)
    .configs(IntegrationTest)
    .settings(exampleJvmCommonSettings: _*)
    .settings(Defaults.itSettings: _*)
    .settings(
      scalaJSProjects := Seq(exampleE2eWebSocketJs),
      unmanagedResourceDirectories in Assets ++= Seq(
        (baseDirectory in exampleE2eWebSocketJs).value / "src" / "main" / "public"
      ),
      libraryDependencies ++= Seq(
        "org.eclipse.jetty.websocket" % "websocket-api" % JettyVersion,
        "org.eclipse.jetty.websocket" % "websocket-server" % JettyVersion
      ),
      (fork in IntegrationTest) := true,
      (javaOptions in IntegrationTest) ++= Seq(
        s"-DjarLocation=${assembly.value}"
      )
    )
    .dependsOn(exampleTestUtils.jvm % "it")

lazy val exampleE2eWebSocketJs = exampleE2eWebSocket.js
    .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
    .disablePlugins(AssemblyPlugin)
    .settings(exampleJsCommonSettings: _*)
    .settings(
      scalaJSUseMainModuleInitializer := true
    )

// Test Utils

lazy val exampleTestUtils = crossProject(JSPlatform, JVMPlatform).in(file("examples/test-utils"))
    .disablePlugins(AssemblyPlugin)
    .settings(commonSettings: _*)
    .settings(exampleCommonSettings: _*)
    .settings(
      name += "-test-utils",
      libraryDependencies ++= Seq(
        "org.apache.httpcomponents" % "httpclient" % "4.+"
      )
    )
*/
