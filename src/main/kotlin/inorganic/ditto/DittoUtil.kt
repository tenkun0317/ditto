package inorganic.ditto

import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.PlainTextContent
import net.minecraft.text.Text
import net.minecraft.text.TranslatableTextContent

object DittoUtil {
    /**
     * Tries to get a stable identifier for a Text object.
     * Prefers translation keys over literal strings to maintain choices across language changes.
     */
    @JvmStatic
    fun getIdentifier(text: Text): String {
        val content = text.content
        if (content is TranslatableTextContent) {
            return content.key
        }
        
        // If literal, the first component's content is often the static part (e.g. "Delete world ")
        if (content is PlainTextContent && content.string().isNotBlank()) {
            return content.string()
        }
        
        // If the root is empty, check siblings for a stable prefix
        for (sibling in text.siblings) {
            val siblingContent = sibling.content
            if (siblingContent is TranslatableTextContent) return siblingContent.key
            if (siblingContent is PlainTextContent && siblingContent.string().isNotBlank()) {
                return siblingContent.string()
            }
        }

        return text.string
    }

    /**
     * Calculates the Y position for the "Don't show again" checkbox.
     * It attempts to place it just above the topmost button in the bottom half of the screen.
     */
    @JvmStatic
    fun getCheckboxY(screen: Screen): Int {
        var minButtonY = screen.height
        var foundButton = false

        for (element in screen.children()) {
            if (element is ClickableWidget) {
                // Look for buttons in the bottom half of the screen
                if (element.y > screen.height / 2) {
                    if (element.y < minButtonY) {
                        minButtonY = element.y
                    }
                    foundButton = true
                }
            }
        }

        return if (foundButton) {
            val y = minButtonY - 24
            // Ensure it doesn't overlap with the center content too much
            if (y < screen.height / 2) screen.height - 30 else y
        } else {
            screen.height - 40
        }
    }
}
