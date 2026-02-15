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
    
    data class ConfigData(
        var bypassKeyCode: Int = 340, // GLFW_KEY_LEFT_SHIFT
        var choices: MutableList<SavedChoice> = mutableListOf()
    )

    private var data = ConfigData()

    val bypassKeyCode: Int get() = data.bypassKeyCode
    val allChoices: List<SavedChoice> get() = data.choices

    fun load() {
        if (!configFile.exists()) return
        
        try {
            val content = configFile.readText()
            if (content.isBlank()) return
            
            val jsonElement = com.google.gson.JsonParser.parseString(content)
            if (jsonElement.isJsonArray) {
                // Migrate from old array-based format
                val loaded = GSON.fromJson(jsonElement, Array<SavedChoice>::class.java)
                data.choices = loaded.toMutableList()
                save() // Save in new format
            } else if (jsonElement.isJsonObject) {
                data = GSON.fromJson(jsonElement, ConfigData::class.java)
            }
        } catch (e: Exception) {
            logger.error("Failed to load Ditto configuration", e)
        }
    }

    fun save() {
        try {
            configFile.writeText(GSON.toJson(data))
        } catch (e: Exception) {
            logger.error("Failed to save Ditto configuration", e)
        }
    }

    fun getChoice(title: String, message: String): Boolean? = 
        data.choices.find { it.title == title && it.message == message }?.choice

    fun setChoice(title: String, message: String, choice: Boolean) {
        data.choices.removeIf { it.title == title && it.message == message }
        data.choices.add(SavedChoice(title, message, choice))
        save()
    }

    fun clearAll() {
        data.choices.clear()
        save()
    }

    fun setChoices(newChoices: List<SavedChoice>) {
        data.choices = newChoices.toMutableList()
    }

    fun setBypassKeyCode(code: Int) {
        data.bypassKeyCode = code
    }
}
