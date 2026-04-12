/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

import sbt.{ExclusionRule, _}

object Dependencies {

  def scalaReflection(scalaVersion: String): Seq[ModuleID] =
    Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion
    )

  // Exclusions

  val jacksonExclusions = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.databind"),
    ExclusionRule(organization = "com.fasterxml.jackson.jaxrs"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(
      organization = "com.fasterxml.jackson.dataformat",
      name = "jackson-dataformat-yaml"
    ),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310")
  )

  val jnaExclusions = Seq(ExclusionRule(organization = "net.java.dev.jna"))

  val sparkExclusions = Seq(
    ExclusionRule(organization = "org.apache.spark")
  )

  // Provided

  val jacksonForSpark4 = Seq(
    "com.fasterxml.jackson.core" % "jackson-core" % Versions.jacksonForSpark4 % "provided",
    "com.fasterxml.jackson.core" % "jackson-annotations" % Versions.jacksonForSpark4 % "provided",
    "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jacksonForSpark4 % "provided",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonForSpark4 % "provided",
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % Versions.jacksonForSpark4 % "provided",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % Versions.jacksonForSpark4 % "provided"
  )

  val spark4 = Seq(
    "org.apache.spark" %% "spark-core" % Versions.spark4 % "provided" exclude ("com.google.guava", "guava") excludeAll (jacksonExclusions: _*),
    "org.apache.spark" %% "spark-sql" % Versions.spark4 % "provided" exclude ("com.google.guava", "guava") excludeAll (jacksonExclusions: _*),
    "org.apache.spark" %% "spark-hive" % Versions.spark4 % "provided" exclude ("com.google.guava", "guava") excludeAll (jacksonExclusions: _*),
    "org.apache.spark" %% "spark-mllib" % Versions.spark4 % "provided" exclude ("com.google.guava", "guava") excludeAll (jacksonExclusions: _*),
    "org.apache.spark" %% "spark-sql-kafka-0-10" % Versions.spark4 excludeAll (jacksonExclusions: _*),
    "org.apache.spark" %% "spark-avro" % Versions.spark4 excludeAll (jacksonExclusions: _*),
    "io.delta" %% "delta-spark" % Versions.deltaSpark4d0 % "provided" exclude ("com.google.guava", "guava") excludeAll (jacksonExclusions: _*)
  )

  val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % Versions.scalatest % Test excludeAll (jacksonExclusions: _*),
    "org.scalatestplus" %% "scalacheck-1-17" % Versions.scalacheckForScalatest % Test excludeAll (jacksonExclusions: _*)
  )

  val logging = Seq(
    "com.typesafe" % "config" % Versions.typesafeConfig,
    "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging
  )

  val starlake = Seq("ai.starlake" %% "starlake-core" % "1.5.3-SNAPSHOT" % "provided")

  val dependencies = logging ++ scalaTest ++ starlake ++ spark4 ++ jacksonForSpark4
}
