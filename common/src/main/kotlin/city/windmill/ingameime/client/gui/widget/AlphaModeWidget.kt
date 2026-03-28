package city.windmill.ingameime.client.gui.widget

import city.windmill.ingameime.client.jni.ExternalBaseIME
import kotlinx.coroutines.*
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.language.I18n
import java.lang.ref.WeakReference

class AlphaModeWidget(font: Font) : Widget(font) {
    private val text get() = I18n.get(if (ExternalBaseIME.AlphaMode) "alpha.ingameime.mode" else "native.ingameime.mode")
    private var hideDelay: WeakReference<Job>? = null

    @OptIn(DelicateCoroutinesApi::class)
    override var active = false
        set(value) {
            hideDelay?.get()?.cancel()
            if (value) {
                hideDelay = WeakReference(GlobalScope.launch {
                    delay(3 * 1000)
                    field = false
                })
            }
            field = value
        }
    override val width
        get() = with(super.width + font.width(text)) {
            if (this < height) height else this
        }
    override val height
        get() = super.height + font.lineHeight
    override val padding: Pair<Int, Int>
        get() = 2 to 3

    override fun draw(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int, delta: Float) {
        super.draw(guiGraphics, offsetX, offsetY, mouseX, mouseY, delta)
        // Add 1 pixel to the right to compensate for Minecraft font's invisible 1px right-kerning
        val textOffsetX = offsetX + (width - font.width(text)) / 2 + 1
        // Add 1 pixel to the bottom to visually center the text's ink mass since minecraft's font baseline has empty bottom pixels
        val textOffsetY = offsetY + padding.second + 1
        guiGraphics.drawString(font, text, textOffsetX, textOffsetY, textColor, false)
    }
}