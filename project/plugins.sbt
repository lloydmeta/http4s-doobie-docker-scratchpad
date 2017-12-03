val neoScalafmtVersion = "1.14"
addSbtPlugin("com.lucidchart"      % "sbt-scalafmt"          % neoScalafmtVersion)
addSbtPlugin("com.lucidchart"      % "sbt-scalafmt-coursier" % neoScalafmtVersion)
addSbtPlugin("io.get-coursier"     % "sbt-coursier"          % "1.0.0-RC13")
addSbtPlugin("org.wartremover"     % "sbt-wartremover"       % "2.2.1")
addSbtPlugin("com.github.tkawachi" % "sbt-doctest"           % "0.7.1")
addSbtPlugin("com.typesafe.sbt"    % "sbt-native-packager"   % "1.3.2")
addSbtPlugin("com.tapad"           % "sbt-docker-compose"    % "1.0.30")
