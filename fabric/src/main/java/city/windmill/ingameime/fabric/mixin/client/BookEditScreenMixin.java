package city.windmill.ingameime.fabric.mixin.client;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import kotlin.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {
    @Shadow
    private @Nullable MultiLineEditBox page;

    protected BookEditScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init*", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(0, 0));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onCaret_BookTail(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (page == null) return;

        MultilineTextField textField = page.textField;
        Font font = page.font;

        int innerTop = page.getInnerTop();
        int innerLeft = page.getInnerLeft();

        int cursor = textField.cursor();
        String fullText = textField.value();
        int currentY = innerTop;
        int finalX = innerLeft;
        int finalY = innerTop;

        for (MultilineTextField.StringView lineView : textField.iterateLines()) {
            int begin = lineView.beginIndex;
            int end = lineView.endIndex;

            if (cursor >= begin && cursor <= end) {
                String lineToCursor = fullText.substring(begin, cursor);
                finalX = innerLeft + font.width(lineToCursor);
                finalY = currentY;
                break;
            }
            currentY += 9; // Line height
        }

        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(finalX, finalY));
    }
}
