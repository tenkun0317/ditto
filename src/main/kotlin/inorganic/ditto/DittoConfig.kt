package inorganic.ditto

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.io.File

object DittoConfig {
    private val logger = LoggerFactory.getLogger("DittoConfig")
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File(FabricLoader.getInstance().configDir.toFile(), "ditto.json")
    
    data class SavedChoice(val title: String, val message: String, val choice: Boolean)
    
    private var choicesMap: MutableMap<Pair<String, String>, Boolean> = mutableMapOf()

    val allChoices: List<SavedChoice> 
        get() = choicesMap.map { SavedChoice(it.key.first, it.key.second, it.value) }

    fun load() {
        if (!configFile.exists()) return
        
        try {
            val content = configFile.readText()
            if (content.isBlank()) return
            
            val loaded = GSON.fromJson(content, Array<SavedChoice>::class.java)
            choicesMap = loaded.associate { (it.title to it.message) to it.choice }.toMutableMap()
        } catch (e: Exception) {
            logger.error("Failed to load Ditto configuration", e)
        }
    }

    fun save() {
        try {
            configFile.writeText(GSON.toJson(allChoices))
        } catch (e: Exception) {
            logger.error("Failed to save Ditto configuration", e)
        }
    }

    fun getChoice(title: String, message: String): Boolean? = choicesMap[title to message]

    fun setChoice(title: String, message: String, choice: Boolean) {
        choicesMap[title to message] = choice
        save()
    }

    fun clearAll() {
        choicesMap.clear()
        save()
    }

    fun setChoices(newChoices: List<SavedChoice>) {
        choicesMap = newChoices.associate { (it.title to it.message) to it.choice }.toMutableMap()
    }
}
