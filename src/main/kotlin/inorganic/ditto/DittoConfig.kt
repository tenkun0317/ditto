package inorganic.ditto

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File

object DittoConfig {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File(FabricLoader.getInstance().configDir.toFile(), "ditto.json")
    
    data class SavedChoice(val title: String, val message: String, val choice: Boolean)
    
    private var choicesMap: MutableMap<Pair<String, String>, Boolean> = mutableMapOf()

    val allChoices: List<SavedChoice> 
        get() = choicesMap.map { SavedChoice(it.key.first, it.key.second, it.value) }

    fun load() {
        if (configFile.exists()) {
            try {
                val loaded = GSON.fromJson(configFile.readText(), Array<SavedChoice>::class.java)
                choicesMap = loaded.associate { (it.title to it.message) to it.choice }.toMutableMap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun save() {
        try {
            val list = allChoices
            configFile.writeText(GSON.toJson(list))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getChoice(title: String, message: String): Boolean? {
        return choicesMap[title to message]
    }

    fun setChoice(title: String, message: String, choice: Boolean) {
        choicesMap[title to message] = choice
        save()
    }

    fun setChoices(newChoices: List<SavedChoice>) {
        choicesMap = newChoices.associate { (it.title to it.message) to it.choice }.toMutableMap()
    }
}
