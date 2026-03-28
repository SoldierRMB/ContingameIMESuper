package city.windmill.ingameime.fabric.mixin.client;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget {
    @Shadow
    private boolean bordered;

    @Shadow
    private boolean isEditable;

    @Shadow
    private int displayPos;

    @Shadow
    public abstract String getValue();

    @Shadow
    public abstract int getCursorPosition();

    private EditBoxMixin(int i, int j, int k, int l, Component component) {
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

    @Inject(method = "onClick", at = @At("TAIL"))
    private void onFocused(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfo ci) {
        int x = this.getX();
        int y = this.getY();
        int caretX = bordered ? x + 4 : x;
        int caretY = bordered ? y + (height - 8) / 2 : y;
        if (isFocused() && isEditable)
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
        else
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void onCaretTail(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!isFocused() || !isEditable) return;

        String text = this.getValue();
        int cursorPos = this.getCursorPosition();
        int dispPos = this.displayPos;

        if (dispPos > text.length()) dispPos = text.length();
        if (cursorPos > text.length()) cursorPos = text.length();

        int textXOffset = 0;
        if (dispPos >= 0 && dispPos <= cursorPos) {
            String displayedSub = text.substring(dispPos, cursorPos);
            textXOffset = Minecraft.getInstance().font.width(displayedSub);
        }

        int renderX = (this.bordered ? this.getX() + 4 : this.getX()) + textXOffset;
        int renderY = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();

        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(renderX, renderY));
    }
}
