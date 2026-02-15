package inorganic.ditto.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import inorganic.ditto.DittoConfig
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.Modifier
import me.shedaniel.clothconfig2.api.ModifierKeyCode
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen ->
            val builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.ditto.config"))
            
            val entryBuilder = builder.entryBuilder()
            
            // General Category
            val general = builder.getOrCreateCategory(Text.translatable("category.ditto.general"))
            
            var clearAll = false
            var bypassKey = DittoConfig.bypassKeyCode

            general.addEntry(entryBuilder.startKeyCodeField(Text.translatable("option.ditto.bypass_key"), InputUtil.Type.KEYSYM.createFromCode(bypassKey))
                .setDefaultValue(InputUtil.Type.KEYSYM.createFromCode(340))
                .setTooltip(Text.translatable("option.ditto.bypass_key.tooltip"))
                .setKeySaveConsumer { key -> bypassKey = key.code }
                .build())

            general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.ditto.clear_all"), false)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("option.ditto.clear_all.tooltip"))
                .setSaveConsumer { clearAll = it }
                .build())

            // Saved Choices Category
            val savedChoicesCategory = builder.getOrCreateCategory(Text.translatable("category.ditto.saved_choices"))
            val choicesToProcess = DittoConfig.allChoices.map { it.copy() }.toMutableList()
            val deletedIndices = mutableSetOf<Int>()

            if (choicesToProcess.isEmpty()) {
                savedChoicesCategory.addEntry(entryBuilder.startTextDescription(Text.translatable("option.ditto.no_choices")).build())
            }

            choicesToProcess.forEachIndexed { index, choice ->
                val displayTitle = choice.title.replace("minecraft.", "").replace("gui.", "")
                val shortMessage = if (choice.message.length > 50) {
                    choice.message.take(47).replace("\n", " ") + "..."
                } else {
                    choice.message.replace("\n", " ")
                }
                
                val label = Text.literal("[$displayTitle] ").append(Text.literal(shortMessage).formatted(net.minecraft.util.Formatting.GRAY))
                val subCategory = entryBuilder.startSubCategory(label)
                    .setTooltip(Text.literal("Full Title ID: ${choice.title}\nFull Message ID: ${choice.message}"))
                
                subCategory.add(entryBuilder.startTextDescription(Text.translatable("option.ditto.id_info", choice.title)).build())
                
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
                
                savedChoicesCategory.addEntry(subCategory.build())
            }
            
            builder.setSavingRunnable {
                if (clearAll) {
                    DittoConfig.clearAll()
                } else {
                    DittoConfig.setBypassKeyCode(bypassKey)
                    val finalChoices = choicesToProcess.filterIndexed { index, _ -> !deletedIndices.contains(index) }
                    DittoConfig.setChoices(finalChoices)
                    DittoConfig.save()
                }
            }
            
            builder.build()
        }
    }
}
