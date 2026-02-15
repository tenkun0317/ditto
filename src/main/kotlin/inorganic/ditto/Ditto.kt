package inorganic.ditto

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

class Ditto : ModInitializer {
    private val logger = LoggerFactory.getLogger("ditto")

	override fun onInitialize() {
		logger.info("Ditto initialized!")
	}
}
