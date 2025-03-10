import Dependencies.{dependencies, jacksonForSpark3, spark3}
import Versions.pureConfig
import sbt.Tests.{Group, SubProcess}
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.Version.Bump.Next
import xerial.sbt.Sonatype._

lazy val javacCompilerVersion = "11"

javacOptions ++= Seq(
  "-source", javacCompilerVersion,
  "-target", javacCompilerVersion,
  "-Xlint"
)

Test / javaOptions ++= Seq("-Dfile.encoding=UTF-8")

val testJavaOptions = {
  val heapSize = sys.env.get("HEAP_SIZE").getOrElse("4g")
  val extraTestJavaArgs = Seq("-XX:+IgnoreUnrecognizedVMOptions",
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
    "--add-opens=java.base/java.io=ALL-UNNAMED",
    "--add-opens=java.base/java.net=ALL-UNNAMED",
    "--add-opens=java.base/java.nio=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED",
    "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
    "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED",
    "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
    "--add-opens=java.base/sun.nio.cs=ALL-UNNAMED",
    "--add-opens=java.base/sun.security.action=ALL-UNNAMED",
    "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED").mkString(" ")
  s"-Xmx$heapSize -Xss4m -XX:ReservedCodeCacheSize=128m -Dfile.encoding=UTF-8 $extraTestJavaArgs"
    .split(" ").toSeq
}

Test / javaOptions ++= testJavaOptions

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

lazy val scala213 = "2.13.16"

lazy val supportedScalaVersions = List(scala213)

 ThisBuild / crossScalaVersions := supportedScalaVersions

organization := "ai.starlake"

organizationName := "starlake"

ThisBuild / scalaVersion := scala213

organizationHomepage := Some(url("https://github.com/starlake-ai/starlake"))

resolvers ++= Resolvers.allResolvers

libraryDependencies ++= dependencies


name := {
  val sparkNameSuffix = {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => "3"
      case _             => throw new Exception(s"Invalid Scala Version")
    }
  }
  s"starlake-streaming"
}

Common.enableStarlakeAliases

enablePlugins(Common.starlakePlugins: _*)


scalacOptions ++= {
  val extractOptions = {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) =>  Seq()
      case _ => throw new Exception(s"Invalid Scala Version")
    }
  }
  Seq(
    "-deprecation",
    "-feature",
    "-Xmacro-settings:materialize-derivations",
    "-Ywarn-unused:imports"
  ) ++ extractOptions

}


Common.customSettings

// Builds a far JAR with embedded spark libraries and other provided libs.
// Can be useful for running YAML generation without having a spark distribution
commands += Command.command("assemblyWithSpark") { state =>
  """set assembly / fullClasspath := (Compile / fullClasspath).value""" :: "assembly" :: state
}


Compile / assembly / artifact := {
  val art: Artifact = (Compile / assembly / artifact).value
  art.withClassifier(Some("assembly"))
}

// Assembly
addArtifact(Compile / assembly / artifact, assembly)
// Required by the Test container framework
Test / fork := true

// Publish
publishTo := {
  (
    sys.env.get("GCS_BUCKET_ARTEFACTS"),
    sys.env.getOrElse("RELEASE_SONATYPE", "true").toBoolean
  ) match {
    case (None, false) =>
      sonatypePublishToBundle.value
    case (None, true) => sonatypePublishToBundle.value
    case (Some(value), _) =>
      Some(GCSPublisher.forBucket(value, AccessRights.InheritBucket))
  }
}
// Disable scaladoc generation

Compile / doc / sources := Seq.empty

//Compile / packageDoc / publishArtifact := false

Compile / packageBin / publishArtifact := true

Compile / packageSrc / publishArtifact := true

// Do not disable checksum
publishLocal / checksums := Nil

// publish / checksums := Nil

// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "ai.starlake"

// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// Open-source license of your choice
licenses := Seq(
  "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")
)

sonatypeProjectHosting := Some(
  GitHubHosting("starlake-ai", "starlake", "hayssam.saleh@starlake.ai")
)

// Release
releaseCrossBuild := true

releaseIgnoreUntrackedFiles := true

releaseProcess := Seq(
//  checkSnapshotDependencies, //allow snapshot dependencies
  inquireVersions,
  runClean,
//  releaseStepCommand("+test"),
  setReleaseVersion,
  commitReleaseVersion, // forces to push dirty files
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

releaseCommitMessage := s"Release ${ReleasePlugin.runtimeVersion.value}"

releaseVersionBump := Next

developers := List(
  Developer(
    id = "hayssams",
    name = "Hayssam Saleh",
    email = "hayssam@saleh.fr",
    url = url("https://github.com/hayssams")
  ),
  Developer(
    id = "elarib",
    name = "Abdelhamide Elarib",
    email = "elarib.abdelhamide@gmail.com",
    url = url("https://github.com/elarib")
  ),
  Developer(
    id = "cchepelov",
    name = "Cyrille Chepelov",
    email = "cyrille@chepelov.org",
    url = url("https://github.com/cchepelov")
  ),
  Developer(
    id = "AmineSagaama",
    name = "Amine Sagaama",
    email = "amine.sagaama@gmail.com",
    url = url("https://github.com/AmineSagaama")
  ),
  Developer(
    id = "mhdqassir",
    name = "Mohamad Kassir",
    email = "mbkassir@gmail.com",
    url = url("https://github.com/mhdkassir")
  ),
  Developer(
    id = "mmenestret",
    name = "Martin Menestret",
    email = "martinmenestret@gmail.com",
    url = url("https://github.com/mmenestret")
  ),
  Developer(
    id = "pchalcol",
    name = "Patice Chalcol",
    email = "pchalcol@gmail.com",
    url = url("https://github.com/pchalcol")
  ),
  Developer(
    id = "zedach",
    name = "Mourad Dachraoui",
    email = "mourad.dachraoui@gmail.com",
    url = url("https://github.com/zedach")
  ),
  Developer(
    id = "seyguai",
    name = "Nicolas Boussuge",
    email = "nb.seyguai@gmail.com",
    url = url("https://github.com/seyguai")
  )
)

//assembly / logLevel := Level.Debug

val packageSetup = Def.taskKey[Unit]("Package Setup.class")
packageSetup := {
  import java.nio.file.Paths
  def zipFile(from: List[java.nio.file.Path], to: java.nio.file.Path): Unit = {
    import java.util.jar.Manifest
    val manifest = new Manifest()
    manifest.getMainAttributes().putValue("Manifest-Version", "1.0")
    manifest.getMainAttributes().putValue("Implementation-Version", version.value)
    manifest.getMainAttributes().putValue("Implementation-Vendor", "starlake")
    manifest.getMainAttributes().putValue("Implementation-Vendor", "starlake")
    manifest.getMainAttributes().putValue("Compiler-Version", javacCompilerVersion)

    IO.jar(from.map(f => f.toFile -> f.toFile.getName()), to.toFile, manifest)

  }
  val scalaMajorVersion = scalaVersion.value.split('.').take(2).mkString(".")
  val setupClass = Paths.get(s"target/scala-$scalaMajorVersion/classes/Setup.class")
  val setupAuthenticatorClass = Paths.get(s"target/scala-$scalaMajorVersion/classes/Setup$$UserPwdAuth.class")
  val setupJarDependencyClass = Paths.get(s"target/scala-$scalaMajorVersion/classes/Setup$$ResourceDependency.class")
  val to = Paths.get("distrib/setup.jar")
  zipFile(
    List(setupClass, setupAuthenticatorClass, setupJarDependencyClass),
    to
  )
}

Compile / packageBin := ((Compile / packageBin).dependsOn(packageSetup)).value


Test / parallelExecution := false

// We want each test to run using its own spark context
Test / testGrouping :=  (Test / definedTests).value.map { suite =>
  Group(suite.name, Seq(suite), SubProcess(ForkOptions().withRunJVMOptions(testJavaOptions.toVector)))
}


