package inorganic.ditto

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File

object DittoConfig {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File(FabricLoader.getInstance().configDir.toFile(), "ditto.json")
    
    data class SavedChoice(val title: String, val message: String, val choice: Boolean)
    
    private var choices: MutableList<SavedChoice> = mutableListOf()

    fun load() {
        if (configFile.exists()) {
            try {
                val loaded = GSON.fromJson(configFile.readText(), Array<SavedChoice>::class.java)
                choices = loaded.toMutableList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun save() {
        try {
            configFile.writeText(GSON.toJson(choices))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getChoice(title: String, message: String): Boolean? {
        return choices.find { it.title == title && it.message == message }?.choice
    }

    fun setChoice(title: String, message: String, choice: Boolean) {
        choices.removeIf { it.title == title && it.message == message }
        choices.add(SavedChoice(title, message, choice))
        save()
    }
}
