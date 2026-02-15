package inorganic.ditto

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.NoticeScreen
import net.minecraft.text.Text

class DittoClient : ClientModInitializer {
	override fun onInitializeClient() {
        DittoConfig.load()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommandManager.literal("dittotest")
                .then(ClientCommandManager.argument("id", IntegerArgumentType.integer(1, 2))
                    .executes { context ->
                        val id = IntegerArgumentType.getInteger(context, "id")
                        val client = MinecraftClient.getInstance()
                        
                        client.execute {
                            when (id) {
                                1 -> client.setScreen(ConfirmScreen(
                                    { res -> 
                                        client.player?.sendMessage(Text.literal("Confirm result: $res"), false)
                                        client.setScreen(null)
                                    },
                                    Text.literal("Test Confirm"),
                                    Text.literal("Do you want to remember this choice?")
                                ))
                                2 -> client.setScreen(NoticeScreen(
                                    { 
                                        client.player?.sendMessage(Text.literal("Notice acknowledged"), false)
                                        client.setScreen(null)
                                    },
                                    Text.literal("Test Notice"),
                                    Text.literal("This notice can also be remembered.")
                                ))
                            }
                        }
                        1
                    }
                )
            )
        }
	}
}
