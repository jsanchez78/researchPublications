import com.typesafe.config.Config

import scala.jdk.CollectionConverters.CollectionHasAsScala

class Settings(config: Config) {

  val xmlInputStartTags: List[String] = getStringList("xml-input.start-tags")
  val xmlInputEndTags: List[String] = getStringList("xml-input.end-tags")

  private def getStringList(path: String): List[String] = config.getStringList(configPath(path)).asScala.toList
  /** Prefixes root key to the specified path to avoid typing it each time a parameter is fetched. */
  private def configPath(path: String): String = s"${Settings.CONFIG_NAMESPACE}.$path"
}

/** Companion object to define "static" members for SimulationConfig class */
object Settings {

  /** Root key for simulation-related configuration */
  val CONFIG_NAMESPACE: String = "CS441"

}