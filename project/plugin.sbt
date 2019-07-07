resolvers ++= DefaultOptions.resolvers(snapshot = true)

addSbtPlugin("com.typesafe.play" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "2.7.0"))
addSbtPlugin("com.typesafe.play" % "interplay" % sys.props.get("interplay.version").getOrElse("2.0.5"))
//2019/06/02 会社の環境で動かなかったため一時的に解除してみる
//addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.3.0")

//addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
//addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
