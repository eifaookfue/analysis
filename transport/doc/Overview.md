| プログラム名 | 概要                                                         | 目的                               |
| ------------ | ------------------------------------------------------------ | ---------------------------------- |
| Bringin      | キャッシュフォルダの中にある指定された日付以降のファイルが含まれるフォルダをすべてコピーする | 会社少しずつファイルを持ち込むため |

### ivysettings.xml

- ivysettings.xmlの中でivysettings-local-scala.xmlをincludeしている
- ivysettings-local-scala.xmlの中では、resolverとしてfilesystem、nameとしてlocal-scalaを設定している
- local-scalaでは、[organisation]/[module]/scala_${scalaVersion}/sbt_${sbtVersion}/([branch]/)[revision]/[type]s/[artifact].[ext]
  という設定
- 生成するbuid.xmlのto属性にlocal-scalaを指定する



### scala-compiler

- C:\Apl\.ivy2\cache\org.scala-lang\scala-compiler\ivydata-2.12.7.propertiesをみると、srcはpublic、ivyはlocalとなっている
- localの参照先はorg.scala-lang\scala-compiler\scala_2.12\ではなく、下記のとおり
- このsrcをローカルに配置したい！！

```
artifact\:scala-compiler\#src\#jar\#1712090559.is-local=false
resolver=sbt-chain
artifact\:scala-compiler\#src\#jar\#1712090559.location=https\://repo1.maven.org/maven2/org/scala-lang/scala-compiler/2.12.7/scala-compiler-2.12.7-sources.jar
```

```
artifact\:ivy\#ivy\#xml\#2112261335.is-local=true
artifact\:ivy\#ivy\#xml\#2112261335.location=C\:\\Apl\\.ivy2\\local\\org.scala-lang\\scala-compiler\\2.12.7\\ivys\\ivy.xml
artifact\:ivy\#ivy\#xml\#2112261335.exists=true

```

- mavenのURLではorganizationがスラッシュで区切られてしまう
- scala-langに関してはhttp://scala-tools.org/repo-releasesからダウンロードする。詳細は下記。

[Ivy custom url resolver](https://stackoverflow.com/questions/3599319/ivy-custom-url-resolver)



