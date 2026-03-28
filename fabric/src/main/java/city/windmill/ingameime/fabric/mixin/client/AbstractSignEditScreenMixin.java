package city.windmill.ingameime.fabric.mixin.client;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import kotlin.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {
    @Shadow
    @Final
    protected SignBlockEntity sign;

    @Shadow
    private int line;

    @Shadow
    private @Nullable TextFieldHelper signField;

    @Shadow
    @Final
    private String[] messages;

    protected AbstractSignEditScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init*", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(0, 0));
    }

    @Inject(method = "renderSignText", at = @At("TAIL"))
    private void onCaret_SignTail(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (signField == null) return;
        String currentLine = messages[line];
        int cursorPos = signField.getCursorPos();

        int textWidth = minecraft.font.width(currentLine);
        int cursorOffset = minecraft.font.width(currentLine.substring(0, Math.min(cursorPos, currentLine.length())));

        float localX = cursorOffset - textWidth / 2.0f;
        float localY = line * sign.getTextLineHeight() - sign.getTextLineHeight() * 4 / 2.0f;

        org.joml.Vector2f transformed = new org.joml.Vector2f(localX, localY);
        guiGraphics.pose().transformPosition(transformed);

        int screenX = (int) transformed.x;
        int screenY = (int) transformed.y;

        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(screenX, screenY));
    }
}
