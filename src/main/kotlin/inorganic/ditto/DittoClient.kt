package inorganic.ditto

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.text.Text

class DittoClient : ClientModInitializer {
	override fun onInitializeClient() {
        DittoConfig.load()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommandManager.literal("dittotest")
                .then(ClientCommandManager.argument("id", IntegerArgumentType.integer(1, 3))
                    .executes { context ->
                        val id = IntegerArgumentType.getInteger(context, "id")
                        val client = MinecraftClient.getInstance()
                        
                        client.execute {
                            val messageBuilder = StringBuilder()
                            val lines = id * 5
                            for (i in 1..lines) {
                                messageBuilder.append("This is test line $i for dialog $id.\n")
                            }
                            messageBuilder.append("Do you want to remember this choice?")

                            client.setScreen(ConfirmScreen(
                                { result ->
                                    client.player?.sendMessage(Text.literal("Test $id: Selected $result"), false)
                                    client.setScreen(null)
                                },
                                Text.literal("Test Dialog $id ($lines lines)"),
                                Text.literal(messageBuilder.toString())
                            ))
                        }
                        1
                    }
                )
            )
        }
	}
}
