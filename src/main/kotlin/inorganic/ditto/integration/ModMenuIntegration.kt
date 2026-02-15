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
            
            var clearAll = false
            category.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.ditto.clear_all"), false)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("option.ditto.clear_all.tooltip"))
                .setSaveConsumer { clearAll = it }
                .build())

            val choicesToProcess = DittoConfig.allChoices.map { it.copy() }.toMutableList()
            val deletedIndices = mutableSetOf<Int>()

            choicesToProcess.forEachIndexed { index, choice ->
                val shortMessage = if (choice.message.length > 30) {
                    choice.message.take(27).replace("\n", " ") + "..."
                } else {
                    choice.message.replace("\n", " ")
                }
                
                val label = "[${choice.title}] $shortMessage"
                val subCategory = entryBuilder.startSubCategory(Text.literal(label))
                
                subCategory.add(entryBuilder.startTextDescription(Text.literal("Full Message: ${choice.message}")).build())

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
                if (clearAll) {
                    DittoConfig.setChoices(emptyList())
                } else {
                    val finalChoices = choicesToProcess.filterIndexed { index, _ -> !deletedIndices.contains(index) }
                    DittoConfig.setChoices(finalChoices)
                }
                DittoConfig.save()
            }
            
            builder.build()
        }
    }
}
