/*
package jp.co.nri.nefs.tool.transport

import java.nio.file.{Files, Path, Paths}
import scala.collection.JavaConverters._

case class Artifact(organisation: String, module: String, revision: String)

object CopyCache {

  val FILE_PATH = "D:\\tmp\\localize.txt"
  val CACHE_DIR = "D:\\Apl\\.ivy2\\cache"
  val LOCAL_DIR = "D:\\tmp"

  def ofArtifact(str: String): Artifact = {
    val strs = str.split("#")
    Artifact(strs(0), strs(1), strs(2))
  }

  def createDir(artifact: Artifact): Unit = {
    Files.createDirectories(Paths.get("D:\\tmp", artifact.organisation, artifact.module, artifact.revision))
  }

  /*
  ■IvyCache
  org.jetbrains\annotations\ivy-13.0.xml CACHE_DIR\ivy-[revision].xml
  ■IvyLocal
  org.jetbrains\annotations\13.0\ivy.xml LOCAL_DIR\ivy.xml

  ■JarCache
  org.jetbrains\annotations\jars\annotations-13.0.jar CACHE_DIR\jars\[module]-[revision].jar
  ■JarLocal
  org.jetbrains\annotations\13.0\jars\annotations.jar LOCAL_DIR\jars\[module].jar
   */
  def copyModules(artifact: Artifact): Unit = {

    val cacheBase = Paths.get(CACHE_DIR, artifact.organisation, artifact.module)
    val targetBase = Paths.get(LOCAL_DIR, artifact.organisation, artifact.module, artifact.revision)
    // Ivy
    val srcIvy = cacheBase.resolve("ivy-" + artifact.revision + ".xml")
    val targetIvy = targetBase.resolve("ivys\\ivy.xml")
    copy(srcIvy, targetIvy)
    // Jar
    val srcJar = cacheBase.resolve("jars\\" + artifact.module + "-" + artifact.revision + ".jar")
    val targetJar = targetBase.resolve("jars\\" + artifact.module + ".jar")
    copy(srcJar, targetJar)
    // Source
    val srcSrc = cacheBase.resolve("srcs\\" + artifact.module + "-" + artifact.revision + ".jar")
    val targetSrc = targetBase.resolve("srcs\\" + artifact.module + ".jar")
    copy(srcSrc, targetSrc)
    // Doc
    val srcDoc = cacheBase.resolve("docs\\" + artifact.module + "-" + artifact.revision + ".jar")
    val targetDoc = targetBase.resolve("docs\\" + artifact.module + ".jar")
    copy(srcDoc, targetDoc)

  }

  def copy(src: Path, target: Path): Unit = {
    if (src.toFile.exists()) {
      Files.createDirectories(target.getParent)
      Files.copy(src,target)
    }  else {
      println(s"$src dose not exist, so skipped copy. ")
    }
  }

  def main(args: Array[String]): Unit = {
    val path = Paths.get(FILE_PATH)
    val lines = Files.readAllLines(path).asScala
    for {line <- lines
         artifact = ofArtifact(line)
    } copyModules(artifact)

  }

}
*/
