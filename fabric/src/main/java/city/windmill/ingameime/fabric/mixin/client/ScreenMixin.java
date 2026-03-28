package city.windmill.ingameime.fabric.mixin.client;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "removed", at = @At("TAIL"))
    private void onRemove(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }
}
