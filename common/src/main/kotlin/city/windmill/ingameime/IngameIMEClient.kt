package city.windmill.ingameime

import city.windmill.ingameime.client.event.ClientScreenEventHooks
import city.windmill.ingameime.client.gui.OverlayScreen
import city.windmill.ingameime.client.handler.ConfigHandler
import city.windmill.ingameime.client.handler.IMEHandler
import city.windmill.ingameime.client.handler.KeyHandler
import city.windmill.ingameime.client.handler.ScreenHandler
import city.windmill.ingameime.client.jni.ExternalBaseIME
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientScreenInputEvent
import dev.architectury.platform.Platform
import dev.architectury.platform.client.ConfigurationScreenRegistry
import net.minecraft.client.Minecraft
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object IngameIMEClient {
    const val MODNAME = "ContingameIMESuper"
    const val MODID = "contingameimesuper"
    val LOGGER: Logger = LoggerFactory.getLogger(MODNAME)

    /**
     * Track mouse move
     */
    private var prevX = 0
    private var prevY = 0

    fun registerConfigScreen() {
        ConfigurationScreenRegistry.register(Platform.getMod(MODID)) { parent ->
            ConfigHandler.createConfigScreen().setParentScreen(parent).build()
        }
    }

    fun onInitClient() {
        ConfigHandler.initialConfig()
        ClientGuiEvent.RENDER_POST.register(ClientGuiEvent.ScreenRenderPost { _, matrices, mouseX, mouseY, delta ->
            //Track mouse move here
            if (mouseX != prevX || mouseY != prevY) {
                ClientScreenEventHooks.SCREEN_MOUSE_MOVE.invoker().onMouseMove(prevX, prevY, mouseX, mouseY)

                prevX = mouseX
                prevY = mouseY
            }

            OverlayScreen.render(matrices, mouseX, mouseY, delta)
        })
        ClientScreenEventHooks.SCREEN_MOUSE_MOVE.register(ClientScreenEventHooks.MouseMove { _, _, _, _ ->
            IMEHandler.IMEState.onMouseMove()
        })
        ClientScreenInputEvent.KEY_PRESSED_PRE.register(ClientScreenInputEvent.KeyPressed { _, _, keyEvent ->
            if (KeyHandler.KeyState.onKeyDown(keyEvent.key(), keyEvent.scancode(), keyEvent.modifiers()))
                EventResult.interruptDefault()
            else
                EventResult.pass()
        })
        ClientScreenInputEvent.KEY_RELEASED_PRE.register(ClientScreenInputEvent.KeyReleased { _, _, keyEvent ->
            if (KeyHandler.KeyState.onKeyUp(keyEvent.key(), keyEvent.scancode(), keyEvent.modifiers()))
                EventResult.interruptDefault()
            else
                EventResult.pass()
        })
        ClientScreenEventHooks.WINDOW_SIZE_CHANGED.register(ClientScreenEventHooks.WindowSizeChanged { _, _ ->
            ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
        })
        ClientScreenEventHooks.SCREEN_CHANGED.register(ClientScreenEventHooks.ScreenChanged(ScreenHandler.ScreenState.Companion::onScreenChange))
        ClientScreenEventHooks.EDIT_OPEN.register(ClientScreenEventHooks.EditOpen(ScreenHandler.ScreenState.EditState.Companion::onEditOpen))
        ClientScreenEventHooks.EDIT_CARET.register(ClientScreenEventHooks.EditCaret(ScreenHandler.ScreenState.EditState.Companion::onEditCaret))
        ClientScreenEventHooks.EDIT_CLOSE.register(ClientScreenEventHooks.EditClose(ScreenHandler.ScreenState.EditState.Companion::onEditClose))
    }
}