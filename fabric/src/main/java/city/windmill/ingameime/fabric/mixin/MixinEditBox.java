package city.windmill.ingameime.fabric.mixin;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import kotlin.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EditBox.class)
abstract class MixinEditBox extends AbstractWidget {
    @Shadow
    private boolean bordered;

    @Shadow
    private boolean isEditable;

    private MixinEditBox(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Inject(method = "setFocused", at = @At("HEAD"))
    private void onSelected(boolean selected, CallbackInfo info) {
        int x = this.getX();
        int y = this.getY();
        int caretX = bordered ? x + 4 : x;
        int caretY = bordered ? y + (height - 8) / 2 : y;
        if (selected && isEditable)
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
        else
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }

    @Inject(method = "setEditable", at = @At("HEAD"))
    private void onEditableChange(boolean bl, CallbackInfo ci) {
        int x = this.getX();
        int y = this.getY();
        int caretX = bordered ? x + 4 : x;
        int caretY = bordered ? y + (height - 8) / 2 : y;
        if (!bl)
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
        else if (isFocused())
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
    }

    @Inject(method = "onClick", at = @At(value = "INVOKE",
            target = "net/minecraft/util/Mth.floor(D)I",
            shift = At.Shift.BEFORE,
            ordinal = 0))
    private void onFocused(double d, double e, CallbackInfo ci) {
        int x = this.getX();
        int y = this.getY();
        int caretX = bordered ? x + 4 : x;
        int caretY = bordered ? y + (height - 8) / 2 : y;
        if (isFocused() && isEditable)
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
        else
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }

    @Inject(method = "renderWidget",
            at = @At(value = "INVOKE", target = "java/lang/String.isEmpty()Z", ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onCaret(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, int k, int l, String string, boolean bl, boolean bl2, int m, int n, int o, int p, boolean bl3, int q) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(q, n));
    }
}
