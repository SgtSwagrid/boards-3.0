import sbtcrossproject.{crossProject, CrossType}

ThisBuild / organization := "swagco"
ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version      := "0.1.0"

lazy val server = (project in file("server"))
  .settings(
    name := "boards-server",
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "com.vmunier"        %% "scalajs-scripts"       % "1.1.4",
      "com.typesafe.play"  %% "play-slick"            % "5.0.0",
      "com.typesafe.play"  %% "play-slick-evolutions" % "5.0.0",
      "com.typesafe.slick" %% "slick-codegen"         % "3.3.2",
      "com.typesafe.play"  %% "play-json"             % "2.9.1",
      "com.typesafe.slick" %% "slick-hikaricp"        % "3.3.3",
      "mysql"              %  "mysql-connector-java"  % "8.0.21",
      "org.mindrot"        %  "jbcrypt"               % "0.4",
      guice
    )
  )
  .enablePlugins(PlayScala)
  .dependsOn(common.jvm)

lazy val client = (project in file("client"))
  .settings(
    name := "boards-client",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      "org.scala-js"      %%% "scalajs-dom"     % "1.1.0",
      "org.querki"        %%% "jquery-facade"   % "2.0",
      "me.shadaj"         %%% "slinky-core"     % "0.6.6",
      "me.shadaj"         %%% "slinky-web"      % "0.6.6",
      "com.typesafe.play" %%  "play-json"       % "2.9.1",
      "com.vmunier"       %%  "scalajs-scripts" % "1.1.4"
    )
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(common.js)

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("common"))
  .settings(
		name := "boards-common",
		libraryDependencies ++= Seq(
			"com.typesafe.play" %%% "play-json" % "2.9.1"
    )
  )

onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}