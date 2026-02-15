package inorganic.ditto.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import inorganic.ditto.DittoConfig
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen ->
            val builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.ditto.config"))
            
            val category = builder.getOrCreateCategory(Text.translatable("category.ditto.general"))
            val entryBuilder = builder.entryBuilder()
            
            val choicesToProcess = DittoConfig.allChoices.map { it.copy() }.toMutableList()
            val deletedIndices = mutableSetOf<Int>()

            choicesToProcess.forEachIndexed { index, choice ->
                val label = if (choice.message.length > 50) {
                    "${choice.title}: ${choice.message.take(47)}..."
                } else {
                    "${choice.title}: ${choice.message}"
                }
                
                val subCategory = entryBuilder.startSubCategory(Text.literal(label))
                
                subCategory.add(entryBuilder.startTextDescription(Text.literal(choice.message)).build())

                subCategory.add(entryBuilder.startBooleanToggle(Text.translatable("option.ditto.choice"), choice.choice)
                    .setDefaultValue(choice.choice)
                    .setSaveConsumer { newValue -> 
                        choicesToProcess[index] = choicesToProcess[index].copy(choice = newValue)
                    }
                    .build())
                
                subCategory.add(entryBuilder.startBooleanToggle(Text.translatable("option.ditto.delete"), false)
                    .setDefaultValue(false)
                    .setSaveConsumer { delete -> 
                        if (delete) deletedIndices.add(index)
                    }
                    .build())
                
                category.addEntry(subCategory.build())
            }
            
            builder.setSavingRunnable {
                val finalChoices = choicesToProcess.filterIndexed { index, _ -> !deletedIndices.contains(index) }
                DittoConfig.setChoices(finalChoices)
                DittoConfig.save()
            }
            
            builder.build()
        }
    }
}
